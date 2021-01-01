# debit-credit
The purpose of this project is to build a simple Spring Boot app and deploy it to AWS ECS with Terraform. 
Once deployed the new database will be initialised with test data (with a configurable volume) that then allows for performance tests to be run using Gatling.

## Sub projects
### debit-credit-app
A simple Spring Boot application with REST APIs to
- create customers and accounts, 
- debit and credit those accounts, and
- fetch reports of account balances and transactions.

The application uses a Postgres database.

### loader
Loader is a command line application that generates a randomised set of customer and account data and loads them to the `debit-credit-app` database.
Data volumes are configurable by setting system properties:

- `customer.count` - The number of customers (with one account each) to create. Defaults to 1000000
- `customer.batch` - The number of SQL inserts in each bulk insert operation. Defaults to 10000

To populate the RDS database the `application.yml` needs to be updated with the RDS datasource details generated by Terraform (see below).

### performance tests
This test suite uses Gatling to run calls to the `debit-credit-app` APIs. Source data sets simulate account sets that have
- high contention accounts. A small number of accounts are repeatedly updated (via the debit or credit APIs) with the 
  potential for concurrent operations to overlap, and
- high contention accounts. A large number of accounts are accessed, but where concurrency risk is lower.

### terraform
Builds the AWS infrastructure to host the `debit-credit-app`. By default, these terraform scripts use objects drawn from the AWS free tier,
however changing settings in the `variables.tf` file can increase the capacity of the deployed stack.

This sub-project has a number of related terraform and shell scripts that construct the whole application stack up to running the `debit-credit-app`.

Loading test data and running the performance tests are run manually once the application stack is running.

Before running the terraform build, an S3 bucket needs to be created to hold the terraform state file. Update the `backend.tf` 
file with the bucket details.
Unfortunately, Terraform does not allow for variable interpolation in the `terraform` block.
```
terraform {
  // ...
  backend "s3" {
    // FIXME These values cannot use variables so take care to rename
    key     = "debit_credit.tfstate"    // "${var.project}.tfstate"
    bucket  = "<your-s3-bucket>"        // "${var.bucket}"
    region  = "eu-west-1"               // "${var.aws_region}"
    profile = "tfuser"
  }
  // ...
```

The application stack is built by running
```shell
terraform init
terraform plan
terraform apply
```

The terraform scripts output the following information:
```
Outputs:

alb_hostname = "debit-credit-load-balancer-2103231549.eu-west-1.elb.amazonaws.com"
bastion_public_ip = "52.17.51.184"
bastion_terminal = "ssh -o StrictHostKeyChecking=no -i ~/.ssh/debit-credit ubuntu@52.17.51.184"
db_endpoint = "debit-credit-db.cdul63ugfpxy.eu-west-1.rds.amazonaws.com:5432"
db_tunnel = "ssh -o StrictHostKeyChecking=no -L 15432:debit-credit-db.cdul63ugfpxy.eu-west-1.rds.amazonaws.com:5432 ubuntu@52.17.51.184 -N"
debit-credit-repository = "383210961596.dkr.ecr.eu-west-1.amazonaws.com/debit-credit"
```
Where:
- `alb_hostname` is the public DND name of the `debit-credit-app`.
- `bastion_public_ip` is the address of the bastion server.
- `bastion_terminal` is the shell command for remote access to the bastion server.
- `db_endpoint` is the internal endpoint of the RDS database instance.
- `db_tunnel` is the shell command used to start the SSH tunnel to the database. This command must be run in a separate shell. 
  Terminating this command will close the tunnel.
- `debit-credit-repository` is the name of the Elastic Container Repository (ECR) that holds the Docker image of the `debit-credit-app`.

Once the stack is no longer required all the AWS resource can be torn down with this command.
```shell
terraform destroy
```
Forgetting this step may run up an unexpected bill with AWS.

The terraform scripts build three broad components of the application stack: the network, 

#### Network
Scripts create a dedicated VPC that spans two availability zones (AZ).
Each AZ has a public and private sub-net, with the public subnets being fronted an internet gateway with a public IP.

A small EC2 bastion server sits within a public sub-net and is accessible only via SSH with key-based authentication.
This is used primarily to host an SSH tunnel to the RDS database for data initialisation and PGAdmin access.

#### Application hosting
The `debit-credit-app` is deployed to three Elastic Container Service (ECS) instances with an application load balancer to distribute requests to them.

The memory, CPU and instance count can be adjusted by editing the `variables.tf` file.

Terraform and shell scripts take the Spring Boot application jar and build a Docker image. 
This is then pushed to the ECR for future use when preparing the ECS task definitions.

#### Database
A Postgres RDS instance sits within the main VPC and is visible to the ECS instances via the private sub-nets.
Database setup uses an SSH tunnel via the bastion ECS instance.

Running the SSH tunnel in a separate terminal will allow local applications to connect to the database using `localhost` and 
the tunnel's `local_port` in the connection settings.

