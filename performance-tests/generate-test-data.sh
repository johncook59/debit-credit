#!/bin/bash

DB_HOST="${1:-localhost}"
DB_USER="${2:-postgres}"
DB_NAME="${3:-debit_credit}"

PGPASSWORD="${4:-letmein}" psql --host="${DB_HOST}" --username="${DB_USER}" --dbname="${DB_NAME}" --echo-errors -f test-data.sql

#mv test-data.sql performance-tests/src/gatling/resources/
#mv top_requests.csv performance-tests/src/gatling/resources/
