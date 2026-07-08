# Replace 'gcd' with your %PROJECT-NAME%
project = myproject
# Toolchains and tools
MILL = ./../playground/mill
-include ./../playground/Makefile.include

.PHONY: rtl
rtl: check-firtool
	@fqns=$$(for f in $$(find src/main/scala/$(project) -name '*.scala'); do \
		pkg=$$(grep -oP '(?<=^package\s)[\w.]+' "$$f" | head -1); \
		grep -oP '(?<=object\s)\w+(?=\s+extends\s+App)' "$$f" | while read -r obj; do \
			echo "$$pkg.$$obj"; \
		done; \
	done | sort -u); \

        echo "No 'object ... extends App' entry points found in src/main/scala/$(project)"; \

        exit 1; \
	fi; \
	for fqn in $$fqns; do \
		echo "==> Generating RTL for $$fqn"; \
		$(MILL) $(project).runMain $$fqn || exit 1; \
	done
check: test
.PHONY: test
test: check-firtool ## Run Chisel tests
	$(MILL) $(project).test
	@echo "The VCD file is generated in ./test_run_dir/testname directories."
    
