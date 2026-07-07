## Chisel-RTL Pipeline

A ready-to-use Chisel → RTL pipeline. Clone this repo, drop in your Chisel
design, push — a compiled SystemVerilog release shows up automatically.
No AWS, no manual setup, no infrastructure to manage.

## 1. Clone the repo

```bash
git clone https://github.com/shauryaps-08/chisel-ci-test.git
cd chisel-ci-test
```

## 2. Add your Chisel design

Place your `.scala` file(s) anywhere under `src/main/scala/myproject/`.
Every design needs two things in the same file (or split across files —
it doesn't matter):

1. **The module itself** — your actual hardware logic
2. **An entry-point `object`** — tells the build "generate RTL for this"

```scala
package myproject

import chisel3._

class MyModule extends Module {
  val io = IO(new Bundle {
    // your ports here
  })
  // your logic here
}

object MyModuleMain extends App with emitrtl.Toplevel {
  lazy val topModule = new MyModule
  chisel2firrtl()
  firrtl2sv()
}
```

> Plain `emitVerilog(...)` will **not** work — output must go through the
> `emitrtl.Toplevel` pattern shown above, or it won't be picked up.

### Multiple designs / multiple targets

You are not limited to one design per push. Add as many entry-point
objects as you want — each one produces its own `.sv` output,
automatically, with nothing extra to configure:

```scala
object gcd8 extends App with emitrtl.Toplevel {
  lazy val topModule = new DecoupledGcd(8)
  chisel2firrtl()
  firrtl2sv()
}

object gcd16 extends App with emitrtl.Toplevel {
  lazy val topModule = new DecoupledGcd(16)
  chisel2firrtl()
  firrtl2sv()
}


```

The build scans every file under `src/main/scala/myproject/` for this
pattern and generates RTL for each one it finds. Adding a new target is
just adding a new `object` block and pushing — nothing else changes.

(Optional) Add a test under `src/test/scala/myproject/`:

```scala
package myproject

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MyModuleTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MyModule"

  it should "do the thing" in {
    test(new MyModule) { dut =>
      // your test steps here
    }
  }
}
```

## 3. Commit and push

```bash
git add .
git commit -m "Add my module"
git push origin main
```

That's it. Nothing else to run, register, or configure — every push
triggers the full pipeline automatically.

## 4. What happens automatically

| Stage | Where it runs | What it does |
|---|---|---|
| **CI** | GitHub-hosted runner | Compiles your code, runs lint/format checks |
| **CD** | GitHub-hosted runner | Compiles the full design, runs `firtool` to generate SystemVerilog for every target, packages the result, and publishes it as a new **Release** |

Both stages run entirely on GitHub's own infrastructure — there is no
self-hosted runner or EC2 instance to launch, register, or keep running.
Watch progress under the **Actions** tab.

**Build speed**: the first run after a change to core dependencies will
take longer (full recompile). Subsequent runs reuse cached compiled
output and dependencies, so only your changed files recompile — typically
much faster.

## 5. Getting your RTL

Once CD finishes:

1. Go to the **Releases** section (right sidebar on the repo's Code tab)
2. Open the latest release
3. Download the `.tar.gz` asset

Inside, each target's generated Verilog is at:

```
generated_sv_dir/myproject.<TargetName>.None/chisel_gen_rtl/<TargetName>.sv
```

(One `.sv` per entry-point object you defined — e.g. `gcd8`, `gcd16`,
`fullAdderTarget` would each get their own folder.)

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| A module doesn't show up in the release | It's missing the `object ... extends App with emitrtl.Toplevel` entry point, or isn't under `src/main/scala/myproject/` |
| `Cannot resolve myproject.test` | Same as above — the build can't find a valid target |
| Build fails referencing a class that doesn't exist | An entry-point `object` still references a module you deleted — remove or update it |
| CI fails but your code looks fine | Check the Actions log for the exact compiler/lint error — usually a syntax issue or missing import |
| Push doesn't trigger anything | Confirm you pushed to `main` — CD only runs after CI succeeds on `main` |
