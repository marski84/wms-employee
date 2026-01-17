--liquibase formatted sql

--changeset architect:001_initial_schema
--comment: Initial schema for Employee Module

-- 1. Create Enums
CREATE TYPE employee_role_enum AS ENUM ('EMPLOYEE', 'MANAGER', 'ADMIN');
CREATE TYPE employee_status_enum AS ENUM ('AVAILABLE', 'BUSY', 'SICK_LEAVE', 'HOLIDAY');

-- 2. Create Departments table (manager_id FK added later to avoid cycle)
CREATE TABLE departments
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    manager_id UUID -- Nullable initially
);

-- 3. Create Users table
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    department_id UUID,

    name          VARCHAR(100)         NOT NULL,
    surname       VARCHAR(100)         NOT NULL,
    email         VARCHAR(255)         NOT NULL UNIQUE,
    phone_number  VARCHAR(50),
    password      VARCHAR(255)         NOT NULL,

    job_title     VARCHAR(100),
    role          employee_role_enum   NOT NULL DEFAULT 'EMPLOYEE',
    status        employee_status_enum NOT NULL DEFAULT 'AVAILABLE',

    created_at    TIMESTAMP WITH TIME ZONE      DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE      DEFAULT NOW(),

    CONSTRAINT fk_users_department
        FOREIGN KEY (department_id)
            REFERENCES departments (id)
            ON DELETE SET NULL
);

-- 4. Add Manager FK to Departments (Closing the loop)
ALTER TABLE departments
    ADD CONSTRAINT fk_departments_manager
        FOREIGN KEY (manager_id)
            REFERENCES users (id)
            ON DELETE SET NULL;

-- 5. Create indexes for improved query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);
CREATE INDEX IF NOT EXISTS idx_users_status ON users (status);
CREATE INDEX IF NOT EXISTS idx_users_department ON users (department_id);
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments (name);
CREATE INDEX IF NOT EXISTS idx_departments_manager ON departments (manager_id);

--rollback ALTER TABLE departments DROP CONSTRAINT fk_departments_manager;
--rollback DROP INDEX IF EXISTS idx_departments_manager;
--rollback DROP INDEX IF EXISTS idx_departments_name;
--rollback DROP INDEX IF EXISTS idx_users_department;
--rollback DROP INDEX IF EXISTS idx_users_status;
--rollback DROP INDEX IF EXISTS idx_users_role;
--rollback DROP INDEX IF EXISTS idx_users_email;
--rollback DROP TABLE users;
--rollback DROP TABLE departments;
--rollback DROP TYPE employee_status_enum;
--rollback DROP TYPE employee_role_enum;