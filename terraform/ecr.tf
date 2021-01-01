resource "aws_ecr_repository" "repo" {
  name = var.project

  tags = {
    Environment = var.project
  }
}

output "debit-credit-repository" {
  value = aws_ecr_repository.repo.repository_url
}