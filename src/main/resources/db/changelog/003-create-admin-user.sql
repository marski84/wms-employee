-- Changeset 003: db-admin
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = 'emp_admin') THEN
            CREATE ROLE emp_admin LOGIN PASSWORD 'emp_admin'; -- Użycie CREATE ROLE jest bardziej uniwersalne
            RAISE NOTICE 'Role "emp_admin" created.';
        ELSE
            RAISE NOTICE 'Role "emp_admin" already exists, skipping creation.';
        END IF;
    END
$$;


-- Changeset 003-grants: db-admin
-- Grant connect to database
GRANT CONNECT ON DATABASE employee TO emp_admin;

-- Grant schema usage
GRANT USAGE ON SCHEMA public TO emp_admin;

-- Grant permissions to all tables
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO emp_admin;

-- Grant permissions to all sequences
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO emp_admin;

-- Grant permissions to future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO emp_admin;

-- Grant permissions to future sequences
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO emp_admin;