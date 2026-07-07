# Replace 'gcd' with your %PROJECT-NAME%
project = myproject
# Toolchains and tools
MILL = ./../playground/mill
-include ./../playground/Makefile.include

# Targets
.PHONY: rtl
rtl: check-firtool ## Generates Verilog code from Chisel sources (output to ./generated_sv_dir)
	@objs=$$(grep -rhoP '(?<=object )\w+(?=\s+extends\s+App)' src/main/scala/$(project) | sort -u); \
	if [ -z "$$objs" ]; then \
		echo "No 'object ... extends App' entry points found in src/main/scala/$(project)"; \
		exit 1; \
	fi; \
	for obj in $$objs; do \
		echo "==> Generating RTL for $(project).$$obj"; \
		$(MILL) $(project).runMain $(project).$$obj || exit 1; \
	done

check: test
.PHONY: test
test: check-firtool ## Run Chisel tests
	$(MILL) $(project).test.test
	@echo "The VCD file is generated in ./test_run_dir/testname directories."
