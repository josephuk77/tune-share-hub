#!/bin/bash
set -euo pipefail

if [ -z "${APP_USER:-}" ] || [ -z "${APP_USER_PASSWORD:-}" ]; then
  echo "APP_USER and APP_USER_PASSWORD are required to initialize the schema."
  exit 1
fi

sqlplus -s "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/XEPDB1" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET SQLBLANKLINES ON
SET DEFINE OFF
@/opt/tune-share-hub/schema/001_init_schema.sql
SQL
