# Chisel GCD — RTL CI Pipeline

[![CI](https://github.com/shauryaps-08/chisel-ci-test/actions/workflows/ci.yml/badge.svg)](https://github.com/shauryaps-08/chisel-ci-test/actions/workflows/ci.yml)

A hardware implementation of the **Greatest Common Divisor (GCD)** algorithm written in [Chisel](https://www.chisel-lang.org/), with a fully automated CI pipeline that compiles, tests, and generates synthesizable SystemVerilog on every push.

---

## What's in this repo

| File/Folder | Purpose |
|---|---|
| `src/main/scala/myproject/GCD.scala` | GCD hardware module (Chisel RTL) |
| `src/main/scala/myproject/DecoupledGCD.scala` | GCD with Decoupled (ready/valid) IO |
| `src/main/scala/myproject/Top.scala` | Top-level elaboration entry point |
| `src/main/scala/myproject/SimTime.scala` | Simulation timing utilities |
| `build.sc` | Mill build definition — wires up rocket-chip, diplomacy, chipyard, emitrtl |
| `Makefile` | Shortcuts for running tests and generating RTL |
| `.github/workflows/ci.yml` | GitHub Actions CI pipeline |

---

## Dependency stack

This project builds on top of the [morphingmachines/playground](https://github.com/morphingmachines/playground) framework, which provides a Mill-based Chisel development environment. The full dependency chain is:

```
myproject
  └── emitrtl          (Chisel elaboration driver)
  └── chipyardAnnotations / chipyardTapeout
  └── myrocketchip     (Rocket Chip SoC framework)
        └── mydiplomacy
        └── myhardfloat (Berkeley HardFloat)
        └── mycde       (Config/Diplomacy Engine)
        └── macros
```

---

## Prerequisites (local development)

- **Java 17** (Temurin recommended)
- **Mill 0.11.6** — `echo "0.11.6" > ../playground/.mill-version`
- **firtool 1.62.1** — CIRCT/MLIR-based FIRRTL compiler

Install firtool:
```bash
wget https://github.com/llvm/circt/releases/download/firtool-1.62.1/firrtl-bin-linux-x64.tar.gz
tar -xzf firrtl-bin-linux-x64.tar.gz
sudo mv firtool-1.62.1/bin/firtool /usr/local/bin/
```

Clone the playground and its submodules alongside this repo:
```bash
git clone https://github.com/morphingmachines/playground.git
cd playground
sed -i 's|git@github.com:|https://github.com/|g' .gitmodules
git submodule update --init --depth=1 dependencies/cde
git submodule update --init --depth=1 dependencies/diplomacy
git submodule update --init --depth=1 dependencies/berkeley-hardfloat
git submodule update --init --depth=1 dependencies/rocket-chip
git submodule update --init --depth=1 dependencies/emitrtl
git submodule update --init --depth=1 dependencies/chipyard
```

Your directory layout should look like:
```
parent/
  ├── playground/
  └── chisel-ci-test/   ← this repo
```

---

## Usage

```bash
# Run Chisel tests (generates VCD waveform in ./test_run_dir/)
make check

# Generate SystemVerilog from Chisel sources (output to ./generated_sv_dir/)
make rtl
```

---

## CI Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push and pull request and does the following automatically:

1. Clones `morphingmachines/playground` and initialises all required submodules
2. Sets up Java 17 and installs Mill
3. Downloads and installs firtool
4. Runs `make check` — compiles all Scala sources and runs the GCD test suite
5. Runs `make rtl` — elaborates the design and emits SystemVerilog

---

## Part of CVS Flagship Project

This repository is the Chisel/RTL component of a larger processor design project. The AURA-32 — a custom 32-bit RISC processor with a 7-stage pipeline — is being developed alongside this CI infrastructure.
