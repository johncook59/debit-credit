#!/usr/bin/env bash
#
# Initialises the database schema
#
set -e

local_port=15432

#
# Start the SSH tunnel to the RDS instance via the bastion server, run the SQL script then auto close the tunnel
#
ssh -f -o StrictHostKeyChecking=no -L ${local_port}:${DB_HOST} ubuntu@${BASTION_PUBLIC_IP} sleep 10; \
  psql --host=localhost --username ${DB_USER} --port=${local_port} --dbname=${DB_NAME} --echo-errors --file=${INIT_SCRIPT}
