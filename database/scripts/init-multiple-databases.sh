#!/bin/bash

set -e
set -u

function database_exists() {
    local database=$1
    local exists=$(psql -U "$POSTGRES_USER" -tAc "SELECT 1 FROM pg_database WHERE datname='$database'")
    if [ "$exists" = "1" ]; then
        return 0  # True, database exists
    else
        return 1  # False, database does not exist
    fi
}

function create_user_and_database() {
    local database=$1
    echo "Checking if database '$database' exists..."

    if database_exists "$database"; then
        echo "Database '$database' already exists. Skipping creation."
    else
        echo "Creating database '$database'..."
        psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
            CREATE DATABASE $database;
            GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
        echo "Database '$database' created successfully."
    fi
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Processing databases: $POSTGRES_MULTIPLE_DATABASES"
    for db in $(echo "$POSTGRES_MULTIPLE_DATABASES" | tr ',' ' '); do
        create_user_and_database $db
    done
    echo "Database initialization completed."
fi