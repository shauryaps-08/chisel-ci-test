# Chisel RTL Generation — User Guide

This guide walks you through using the Chisel CI/CD AMI to write a Chisel
module and get synthesizable Verilog out, with zero manual toolchain setup.
Every command below is meant to be copy-pasted into your terminal in order.

---

## 0. Set up AWS CLI access (one-time, skip if already configured)

Install the AWS CLI if you don't have it:
```bash
# Linux
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
aws --version
```
```bash
# macOS
brew install awscli
aws --version
```

Configure your credentials (get the Access Key ID / Secret Access Key from
your AWS account admin, or IAM Console → your user → Security credentials):
```bash
aws configure
```
You'll be prompted for:
```
AWS Access Key ID:     <paste here>
AWS Secret Access Key: <paste here>
Default region name:   ap-south-1
Default output format: json
```

Verify it worked and check which account/identity you're using:
```bash
aws sts get-caller-identity
```

---

## 1. Find the AMI and launch your instance

**Current AMI ID (ap-south-1):**
```
ami-00dee53f8deb9978d
```
Use this directly in the launch command below. If your team creates a
newer AMI later, update this value (or run the lookup command to find the
latest one):
```bash
aws ec2 describe-images \
  --owners self \
  --filters "Name=name,Values=chisel-ci-*" \
  --region ap-south-1 \
  --query 'Images[*].[ImageId,Name,CreationDate]' \
  --output table
```

**Launch an instance from that AMI** (replace `<KEY_NAME>` and
`<SECURITY_GROUP_ID>` with your actual values — ask your team if unsure
which key pair / security group to use):
```bash
aws ec2 run-instances \
  --image-id ami-00dee53f8deb9978d \
  --instance-type t3.medium \
  --key-name <KEY_NAME> \
  --security-group-ids <SECURITY_GROUP_ID> \
  --region ap-south-1 \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=my-chisel-rtl-work}]' \
  --query 'Instances[0].InstanceId' \
  --output text
```
This prints your new **Instance ID** (e.g. `i-0123456789abcdef0`) — save it,
you'll need it below.

**Wait until the instance is running**, then get its public IP:
```bash
aws ec2 describe-instances \
  --instance-ids <INSTANCE_ID> \
  --region ap-south-1 \
  --query 'Reservations[0].Instances[0].[State.Name,PublicIpAddress]' \
  --output table
```
Wait until `State.Name` shows `running` and a `PublicIpAddress` appears
(can take ~30-60 seconds after launch).

**Connect to it via SSH:**
```bash
ssh -i /path/to/<KEY_NAME>.pem ubuntu@<PUBLIC_IP>
```

Alternatively, use **EC2 Instance Connect** (no key file needed) directly
from the AWS Console: EC2 → Instances → select your instance → **Connect**
→ EC2 Instance Connect → Connect.

---

## 2. Create your project repo

---

## 2. Create your project repo

If you don't already have a GitHub repo for your Chisel project, create one
on GitHub first (empty is fine), then clone it:
```bash
git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>
```

---

## 3. Copy in the pre-baked CI/CD template

```bash
cp /opt/chisel-ci-template/Makefile ./
cp -r /opt/chisel-ci-template/.github ./
cp /opt/chisel-ci-template/.scalafix.conf ./
cp /opt/chisel-ci-template/.scalafmt.conf ./
cp /opt/chisel-ci-template/build.sc ./
```

If your project name isn't `myproject`, open `build.sc` and `Makefile` and
replace `myproject` with your actual project name throughout.

---

## 4. Write your Chisel module

Create the source directory and your module file:
```bash
mkdir -p src/main/scala/myproject
nano src/main/scala/myproject/mymodule.scala
```

Your module **must** follow this exact pattern to be auto-discovered by
`make rtl`:
```scala
package myproject
import chisel3._

class MyModule extends Module {
  val io = IO(new Bundle {
    val in  = Input(UInt(3.W))
    val out = Output(UInt(8.W))
  })
  io.out := (1.U << io.in)
}

object MyModuleMain extends App with emitrtl.Toplevel {
  lazy val topModule = new MyModule
  chisel2firrtl()
  firrtl2sv()
}
```
Do **not** use plain `emitVerilog(...)` — it writes output outside
`generated_sv_dir/`, which the release packaging step won't see.

---

## 5. Write a test for your module

Create the test directory and file:
```bash
mkdir -p src/test/scala/myproject
nano src/test/scala/myproject/MyModuleTest.scala
```

Example test:
```scala
package myproject

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class MyModuleTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "MyModule"

  it should "produce a one-hot output for every 3-bit input" in {
    test(new MyModule) { dut =>
      for (i <- 0 until 8) {
        dut.io.in.poke(i.U)
        dut.clock.step(1)
        dut.io.out.expect((1 << i).U)
      }
    }
  }
}
```

`make check` will fail if this directory is empty or missing — this is
intentional. Do not delete or skip this step.

---

## 6. Test locally before pushing (recommended)

```bash
./../playground/mill myproject.test    # runs your tests
./../playground/mill myproject.fix     # runs lint (scalafix)
```
Fix any errors before pushing. If both pass, you're ready for step 7.

---

## 7. Register this instance as a GitHub Actions runner (one-time per repo)

On GitHub: your repo → **Settings → Actions → Runners → New self-hosted
runner** → select Linux/x64 → copy the generated token (expires in ~1 hour,
use it right away).

```bash
mkdir actions-runner && cd actions-runner
curl -o actions-runner-linux-x64-<version>.tar.gz -L <download-url-from-github>
tar xzf ./actions-runner-linux-x64-<version>.tar.gz
./config.sh --url https://github.com/<your-username>/<your-repo> --token <token>
sudo ./svc.sh install
sudo ./svc.sh start
sudo ./svc.sh status   # should say "active (running)"
```
Go back to your project directory afterward:
```bash
cd ~/<your-repo>
```

---

## 8. Commit and push

```bash
git add .
git commit -m "Add MyModule with test"
git push origin main
```

---

## 9. Watch CI/CD run

On GitHub, go to your repo's **Actions** tab:
1. CI runs first — compiles your code, runs your test (`make check`), runs
   lint (`make lint`). All three must pass.
2. If CI passes, CD runs automatically on your self-hosted runner —
   generates the actual Verilog.

---

## 10. Download your RTL

Go to your repo's **Releases** page. Download the `.tar.gz` artifact.
Inside, your Verilog is at:
```
generated_sv_dir/myproject.MyModule.None/chisel_gen_rtl/MyModule.sv
```

---

## Troubleshooting quick reference

| Symptom | Likely cause |
|---|---|
| `make lint` fails with "Something unexpected happened" | `.scalafix.conf` missing or has an empty `rules = []` — must contain at least one real rule |
| `make check` fails: "test class not found" | Your test file/class name doesn't exist yet, or isn't under `src/test/scala/<project>/` |
| CD fails: "destination path 'playground' already exists" | Stale leftover from a previous interrupted run on the runner — ask an admin to clear it, or re-run after a clean workspace |
| Generated `.sv` file missing from release | You used `emitVerilog(...)` instead of the `emitrtl.Toplevel` pattern shown in step 4 |

---

## Performance notes

| Instance    | vCPU | RAM  | Approx. build time |
|-------------|------|------|---------------------|
| t3.micro    | 1    | 1GB  | 15-20+ min (swap-bound) |
| t3.medium   | 2    | 4GB  | ~3-5 min |
| t3.large    | 2    | 8GB  | ~2-3 min |
| t3.xlarge   | 4    | 16GB | ~1-2 min |

`t3.medium` or larger is recommended for real use.
