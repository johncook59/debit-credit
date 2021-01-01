variable "aws_region" {
  description = "The AWS region things are created in"
  default     = "eu-west-1"
}

variable "az_count" {
  description = "Number of AZs to cover in a given region"
  default     = "2"
}

variable "source_path" {
  description = "Path to the application to be built to a docker image"
  default     = "../debit-credit-app"
}

variable "docker_image_tag" {
  description = "Tag to use when pushing the docker image to the ecr repository"
  default     = "latest"
}

variable "app_port" {
  description = "Port exposed by the docker image to redirect traffic to"
  default     = 8080
}

variable "app_count" {
  description = "Number of docker containers to run"
  default     = 1
}

variable "ecs_task_execution_role" {
  description = "Role arn for the ecsTaskExecutionRole"
  default     = "arn:aws:iam::035898547283:role/ecsTaskExecutionRole"
}

variable "health_check_path" {
  default = "/health"
}

variable "fargate_cpu" {
  description = "Fargate instance CPU units to provision (1 vCPU = 1024 CPU units)"
  default     = "1024"
}

variable "fargate_memory" {
  description = "Fargate instance memory to provision (in MiB)"
  default     = "2048"
}

# A project name
variable "project" {
  default = "debit-credit"
}

# User name for RDS
variable "db_username" {
  default = "postgres"
}

variable "db_password" {
  default = "letmein1234!!!!"
}

# The DB name in the RDS instance. Note that this cannot contain -'s
variable "db_name" {
  default = "debit_credit"
}

variable "db_port" {
  default = "5432"
}

variable "db_instance_class" {
  default = "db.t2.micro"
}

variable "db_allocated_storage" {
  default = 20
}

variable "db_storage_type" {
  default = "gp2"
}

variable "db_engine" {
  default = "postgres"
}

variable "db_engine_version" {
  default = "11.8"
}

variable "bastion_ami" {
  default = "ami-0dc8d444ee2a42d8a"
}

variable "bastion_instance_type" {
  default = "t2.nano"
}

variable "bastion_key" {
  default = "~/.ssh/debit-credit"
}

variable "db_init_script" {
  description = "SQL to initialise the RDS instance"
  default     = "initial-schema.sql"
}

