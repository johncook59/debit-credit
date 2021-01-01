resource "aws_instance" "bastion" {
  ami                         = var.bastion_ami
  key_name                    = aws_key_pair.bastion_key.key_name
  instance_type               = var.bastion_instance_type
  security_groups             = [aws_security_group.bastion.id]
  subnet_id                   = aws_subnet.public[0].id
  associate_public_ip_address = true

  tags = {
    Name = "Bastion"
    Environment = var.project
  }
}

resource "aws_key_pair" "bastion_key" {
  key_name   = "${var.project}-bastion-key"
  public_key = file("${var.bastion_key}.pub")

  tags = {
    Environment = var.project
  }
}

output "bastion_public_ip" {
  value = aws_instance.bastion.public_ip
}

output "bastion_terminal" {
  value = "ssh -o StrictHostKeyChecking=no -i ${var.bastion_key} ubuntu@${aws_instance.bastion.public_ip}"
}