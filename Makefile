# Replace 'gcd' with your %PROJECT-NAME%
project = myproject

# Toolchains and tools
MILL = ./../playground/mill

-include ./../playground/Makefile.include

# Targets
rtl: check-firtool ## Generates Verilog code from Chisel sources (output to ./generated_sv_dir)
	$(MILL) $(project).runMain myproject.gcd8

check: test
.PHONY: test
test: check-firtool ## Run Chisel tests
	$(MILL) $(project).test.testOnly myproject.GCDSpec
	@echo "The VCD file is generated in ./test_run_dir/testname directories."
