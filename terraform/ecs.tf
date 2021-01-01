resource "aws_ecs_cluster" "main" {
  name = "${var.project}-cluster"

  depends_on = [
    null_resource.init_db
  ]

  tags = {
    Environment = var.project
  }
}

data "template_file" "app_template_file" {
  template = file("templates/ecs/app.json")

  vars = {
    app_name       = "${var.project}-app"
    app_image      = "${aws_ecr_repository.repo.repository_url}:${var.docker_image_tag}"
    fargate_cpu    = var.fargate_cpu
    fargate_memory = var.fargate_memory
    aws_region     = var.aws_region
    app_port       = var.app_port
    db_hostname    = aws_db_instance.postgres.endpoint
    db_name        = var.db_name
    db_username    = var.db_username
    db_password    = var.db_password
    logs_group     = aws_cloudwatch_log_group.log_group.name
  }
}

resource "aws_iam_role" "ecs_task_role" {
  name = "${var.project}-ecsTaskRole"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json

  tags = {
    Environment = var.project
  }
}

data "aws_iam_policy_document" "assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name = "${var.project}-ecsTaskExecutionRole"
  assume_role_policy = data.aws_iam_policy_document.assume_role_policy.json

  tags = {
    Environment = var.project
  }
}

resource "aws_iam_role_policy_attachment" "ecs-task-execution-role-policy-attachment" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project}-app-task"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.fargate_cpu
  memory                   = var.fargate_memory
  container_definitions    = data.template_file.app_template_file.rendered

  tags = {
    Environment = var.project
  }
}

resource "aws_ecs_service" "main" {
  name            = "${var.project}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.app_count
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.ecs_tasks.id]
    subnets          = aws_subnet.private.*.id
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_alb_target_group.app.id
    container_name   = "${var.project}-app"
    container_port   = var.app_port
  }

  depends_on = [
    aws_alb_listener.front_end
  ]

  tags = {
    Environment = var.project
  }
}

resource "aws_cloudwatch_log_group" "log_group" {
  name = "awslogs-${var.project}"

  tags = {
    Environment = var.project
  }
}

resource "null_resource" "push" {
  provisioner "local-exec" {
    command     = "./push-image.sh ${var.source_path} ${aws_ecr_repository.repo.repository_url} ${var.docker_image_tag}"
    interpreter = ["bash", "-c"]
  }

  depends_on = [aws_ecr_repository.repo]
}