# Replace 'gcd' with your %PROJECT-NAME%
project = myproject
# Toolchains and tools
MILL = ./../playground/mill
-include ./../playground/Makefile.include

.PHONY: rtl
rtl: check-firtool ## Generates Verilog from Chisel sources listed in rtl_targets.txt
	@if [ ! -f rtl_targets.txt ]; then \
		echo "rtl_targets.txt not found -- nothing to generate."; \
		exit 1; \
	fi; \
	fqns=$$(grep -v '^\s*#' rtl_targets.txt | grep -v '^\s*$$'); \
	if [ -z "$$fqns" ]; then \
		echo "rtl_targets.txt is empty -- nothing to generate."; \
		exit 1; \
	fi; \
	for fqn in $$fqns; do \
		echo "==> Generating RTL for $$fqn"; \
		$(MILL) $(project).runMain $$fqn || exit 1; \
	done

.PHONY: check-rtl-manifest
check-rtl-manifest: ## Warns (non-blocking) if rtl_targets.txt has drifted from actual source.
                     ## Uses \K instead of a variable-length lookbehind (PCRE requires
                     ## fixed-length lookbehinds), and temp files instead of <(...)
                     ## process substitution (bash-only, not portable to plain /bin/sh).
	@manifest_fqns=$$(grep -v '^\s*#' rtl_targets.txt | grep -v '^\s*$$' | sort -u); \
	found_fqns=$$(for f in $$(find src/main/scala/$(project) -name '*.scala'); do \
		pkg=$$(grep -oP '^\s*package\s+\K[\w.]+' "$$f" | head -1); \
		grep -oP '(?<=object\s)\w+(?=\s+extends\s+App)' "$$f" | while read -r obj; do \
			[ -n "$$pkg" ] && echo "$$pkg.$$obj"; \
		done; \
	done | sort -u); \
	echo "$$manifest_fqns" > /tmp/rtl_manifest_check.manifest; \
	echo "$$found_fqns" > /tmp/rtl_manifest_check.found; \
	missing_from_manifest=$$(comm -13 /tmp/rtl_manifest_check.manifest /tmp/rtl_manifest_check.found); \
	stale_in_manifest=$$(comm -23 /tmp/rtl_manifest_check.manifest /tmp/rtl_manifest_check.found); \
	rm -f /tmp/rtl_manifest_check.manifest /tmp/rtl_manifest_check.found; \
	if [ -n "$$missing_from_manifest" ]; then \
		echo "WARNING: App objects found in source but NOT in rtl_targets.txt (won't be built by 'make rtl'):"; \
		echo "$$missing_from_manifest" | sed 's/^/  - /'; \
	fi; \
	if [ -n "$$stale_in_manifest" ]; then \
		echo "WARNING: rtl_targets.txt lists entries not found via source scan (may be stale, or a formatting quirk this scan can't see -- verify manually):"; \
		echo "$$stale_in_manifest" | sed 's/^/  - /'; \
	fi

check: test
.PHONY: test
test: check-firtool ## Run Chisel tests
	$(MILL) $(project).test
	@echo "The VCD file is generated in ./test_run_dir/testname directories."
