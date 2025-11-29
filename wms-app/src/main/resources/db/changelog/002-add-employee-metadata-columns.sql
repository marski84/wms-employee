-- Add metadata columns to employee table for role, status, and supervisor tracking
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS role VARCHAR(50);
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'REGISTERED';
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS supervisor_id BIGINT;
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS terminated_at TIMESTAMP WITH TIME ZONE;

-- Create indexes for improved query performance
CREATE INDEX IF NOT EXISTS idx_employee_role ON employee (role);
CREATE INDEX IF NOT EXISTS idx_employee_status ON employee (status);
CREATE INDEX IF NOT EXISTS idx_employee_supervisor_id ON employee (supervisor_id);

-- Add foreign key constraint for supervisor (self-referencing)
ALTER TABLE employee
    ADD CONSTRAINT fk_employee_supervisor
        FOREIGN KEY (supervisor_id) REFERENCES employee (id) ON DELETE SET NULL;