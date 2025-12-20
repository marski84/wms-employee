package employee.model.enumeration;

/**
 * Defines the available roles for employees in the system.
 * Maps directly to PostgreSQL ENUM type 'employee_role_enum'.
 */
public enum EmployeeRole {
    EMPLOYEE,
    MANAGER,
    ADMIN
}