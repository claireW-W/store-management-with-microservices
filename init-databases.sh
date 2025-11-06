#!/bin/bash
set -e

echo "Creating databases..."

# Wait for PostgreSQL to be ready
until PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "postgres" -c '\q' 2>/dev/null; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 1
done

# Create databases
PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "postgres" <<-EOSQL
    CREATE DATABASE store_backend_db;
    CREATE DATABASE warehouse_db;
    CREATE DATABASE bank_db;
    CREATE DATABASE delivery_db;
    CREATE DATABASE email_db;
EOSQL

echo "Databases created successfully!"

# Apply schemas if they exist
if [ -d "/docker-entrypoint-initdb.d" ]; then
    for sql_file in /docker-entrypoint-initdb.d/*.sql; do
        if [ -f "$sql_file" ]; then
            db_name=$(basename "$sql_file" .sql)
            echo "Applying schema from $sql_file to $db_name"
            
            case "$db_name" in
                store_backend_db)
                    PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "store_backend_db" < "$sql_file"
                    ;;
                warehouse_db)
                    PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "warehouse_db" < "$sql_file"
                    ;;
                bank_db)
                    PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "bank_db" < "$sql_file"
                    ;;
                delivery_db)
                    PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "delivery_db" < "$sql_file"
                    ;;
                email_db)
                    PGPASSWORD=$POSTGRES_PASSWORD psql -h "localhost" -U "$POSTGRES_USER" -d "email_db" < "$sql_file"
                    ;;
            esac
        fi
    done
fi

echo "Database initialization completed!"

