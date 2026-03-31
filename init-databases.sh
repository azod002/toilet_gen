#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE identity_db;
    CREATE DATABASE toilet_db;
    CREATE DATABASE sos_db;
    CREATE DATABASE gamification_db;
EOSQL
