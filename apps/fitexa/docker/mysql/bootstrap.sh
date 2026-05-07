#!/bin/sh
set -eu

cat \
  /fitexa-bootstrap/00-cleanup.sql \
  /fitexa-bootstrap/01-schema.sql \
  /fitexa-bootstrap/02-seed.sql \
  > /tmp/fitexa-bootstrap.sql

exec /usr/local/bin/docker-entrypoint.sh mysqld --init-file=/tmp/fitexa-bootstrap.sql
