--liquibase formatted sql

--changeset developer:003_change_enum_to_varchar
--comment: Change PostgreSQL ENUM columns to VARCHAR for Hibernate compatibility

-- 1. Drop default values (they depend on ENUM types)
ALTER TABLE users
    ALTER COLUMN role DROP DEFAULT;
ALTER TABLE users
    ALTER COLUMN status DROP DEFAULT;

-- 2. Convert columns from ENUM to VARCHAR
ALTER TABLE users
    ALTER COLUMN role TYPE VARCHAR(50) USING role::text;

ALTER TABLE users
    ALTER COLUMN status TYPE VARCHAR(50) USING status::text;

-- 3. Re-add default values as VARCHAR
ALTER TABLE users
    ALTER COLUMN role SET DEFAULT 'EMPLOYEE';
ALTER TABLE users
    ALTER COLUMN status SET DEFAULT 'AVAILABLE';

-- 4. Add CHECK constraints to maintain data integrity
ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('EMPLOYEE', 'MANAGER', 'ADMIN'));

ALTER TABLE users
    ADD CONSTRAINT chk_users_status
        CHECK (status IN ('AVAILABLE', 'BUSY', 'SICK_LEAVE', 'HOLIDAY'));

-- 5. Drop the now-unused ENUM types (CASCADE handles any remaining dependencies)
DROP TYPE IF EXISTS employee_role_enum CASCADE;
DROP TYPE IF EXISTS employee_status_enum CASCADE;

--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_status;
--rollback ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_role;
--rollback CREATE TYPE employee_role_enum AS ENUM ('EMPLOYEE', 'MANAGER', 'ADMIN');
--rollback CREATE TYPE employee_status_enum AS ENUM ('AVAILABLE', 'BUSY', 'SICK_LEAVE', 'HOLIDAY');
--rollback ALTER TABLE users ALTER COLUMN role TYPE employee_role_enum USING role::employee_role_enum;
--rollback ALTER TABLE users ALTER COLUMN status TYPE employee_status_enum USING status::employee_status_enum;