resource "aws_db_instance" "postgres" {
  identifier                = "${var.project}-db"
  allocated_storage         = var.db_allocated_storage
  storage_type              = var.db_storage_type
  engine                    = var.db_engine
  engine_version            = var.db_engine_version
  instance_class            = var.db_instance_class
  name                      = var.db_name
  username                  = var.db_username
  password                  = var.db_password
  port                      = var.db_port
  publicly_accessible       = false
  vpc_security_group_ids    = [aws_security_group.rds.id]
  db_subnet_group_name      = aws_db_subnet_group.rds.id
  multi_az                  = false
  skip_final_snapshot       = true

  tags = {
    Name  = "${var.project}-db"
    Group = var.project
    Environment = var.project
  }

  depends_on = [aws_instance.bastion]
}

resource "aws_db_subnet_group" "rds" {
  name        = "${var.project}-db"
  description = "${var.project} db group of subnets"
  subnet_ids  = aws_subnet.private.*.id

  tags = {
    Name = "${var.project} DB subnet group"
    Environment = var.project
  }
}

resource "null_resource" "init_db" {
  provisioner "local-exec" {
    command = "./init-db.sh"
    interpreter = ["bash", "-c"]
    environment = {
      PGPASSWORD        = var.db_password
      DB_NAME           = var.db_name
      DB_USER           = var.db_username
      DB_HOST           = aws_db_instance.postgres.endpoint
      INIT_SCRIPT       = "${var.source_path}/${var.db_init_script}"
      BASTION_PUBLIC_IP = aws_instance.bastion.public_ip
    }
  }

  depends_on = [
    aws_instance.bastion,
    aws_db_instance.postgres
  ]
}

output "db_endpoint" {
  value = aws_db_instance.postgres.endpoint
}

output "db_tunnel" {
  value = "ssh -o StrictHostKeyChecking=no -L 15432:${aws_db_instance.postgres.endpoint} ubuntu@${aws_instance.bastion.public_ip} -N"
}
