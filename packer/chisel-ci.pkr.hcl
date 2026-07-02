packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "instance_type" {
  type    = string
  default = "t3.micro"
}

# Pinned tool versions - update ONLY here, deliberately, when the team agrees to upgrade
variable "java_version" {
  type    = string
  default = "17"
}

variable "mill_version" {
  type    = string
  default = "0.11.6"
}

variable "firtool_version" {
  type    = string
  default = "1.62.1"
}

source "amazon-ebs" "chisel_ci" {
  region        = var.aws_region
  instance_type = var.instance_type
  ssh_username  = "ubuntu"

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"] # Canonical
  }

  ami_name        = "chisel-ci-toolchain-{{timestamp}}"
  ami_description = "Chisel CI/CD build environment: Java ${var.java_version}, Mill ${var.mill_version}, firtool ${var.firtool_version} - pinned versions"

  tags = {
    Name          = "chisel-ci-toolchain"
    JavaVersion   = "${var.java_version}"
    MillVersion   = "${var.mill_version}"
    FirtoolVersion = "${var.firtool_version}"
    ManagedBy     = "packer"
  }
}

build {
  name    = "chisel-ci-ami"
  sources = ["source.amazon-ebs.chisel_ci"]

  # Wait for cloud-init to finish before installing anything
  provisioner "shell" {
    inline = [
      "cloud-init status --wait || true",
      "sudo apt-get update -y"
    ]
  }

  # Install Java 17 (Temurin), matching your CI's actions/setup-java step
  provisioner "shell" {
    inline = [
      "sudo apt-get install -y wget curl unzip git build-essential",
      "sudo mkdir -p /etc/apt/keyrings",
      "wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg",
      "echo \"deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print $2}' /etc/os-release) main\" | sudo tee /etc/apt/sources.list.d/adoptium.list",
      "sudo apt-get update -y",
      "sudo apt-get install -y temurin-${var.java_version}-jdk",
      "echo 'export JAVA_HOME=/usr/lib/jvm/temurin-${var.java_version}-jdk-amd64' | sudo tee -a /etc/profile.d/java_home.sh",
      "/usr/lib/jvm/temurin-${var.java_version}-jdk-amd64/bin/java -version"
    ]
  }

  # Install Mill, pinned version
  provisioner "shell" {
    inline = [
      "curl -L https://raw.githubusercontent.com/lefou/millw/0.4.11/millw > /tmp/mill",
      "chmod +x /tmp/mill",
      "sudo mv /tmp/mill /usr/local/bin/mill",
      "echo '${var.mill_version}' | sudo tee /usr/local/share/mill-default-version"
    ]
  }

  # Install firtool, pinned version
  provisioner "shell" {
    inline = [
      "wget -q https://github.com/llvm/circt/releases/download/firtool-${var.firtool_version}/firrtl-bin-linux-x64.tar.gz -O /tmp/firtool.tar.gz",
      "cd /tmp && tar -xzf firtool.tar.gz",
      "sudo mv /tmp/firtool-${var.firtool_version}/bin/firtool /usr/local/bin/firtool",
      "sudo chmod +x /usr/local/bin/firtool",
      "firtool --version"
    ]
  }

  # Version manifest so future devs (and CI) can confirm what's baked in
  provisioner "shell" {
    inline = [
      "echo 'Chisel CI AMI Toolchain Versions' | sudo tee /etc/chisel-ci-toolchain-versions.txt",
      "echo 'Java: ${var.java_version} (Temurin)' | sudo tee -a /etc/chisel-ci-toolchain-versions.txt",
      "echo 'Mill: ${var.mill_version}' | sudo tee -a /etc/chisel-ci-toolchain-versions.txt",
      "echo 'firtool: ${var.firtool_version}' | sudo tee -a /etc/chisel-ci-toolchain-versions.txt",
      "cat /etc/chisel-ci-toolchain-versions.txt"
    ]
  }
}
