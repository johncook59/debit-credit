terraform {
  required_version = ">= 0.13"

  backend "s3" {
    // FIXME These values cannot use variables so take care to rename
    key     = "debit_credit.tfstate" // "${var.project}.tfstate"
    bucket  = "zarg-terraform-state" // "${var.bucket}"
    region  = "eu-west-1"            // "${var.aws_region}"
    profile = "tfuser"
  }

  required_providers {
    aws = {
      version = "~> 3.19.0"
    }

    null = {
      source  = "hashicorp/null"
      version = "~> 3.0.0"
    }

    template = {
      source  = "hashicorp/template"
      version = "~> 2.2.0"
    }
  }
}
