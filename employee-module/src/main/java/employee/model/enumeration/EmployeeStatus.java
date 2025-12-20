package employee.model.enumeration;

/**
 * Defines the availability status of employees.
 * Maps directly to PostgreSQL ENUM type 'employee_status_enum'.
 */
public enum EmployeeStatus {
    AVAILABLE,
    BUSY,
    SICK_LEAVE,
    HOLIDAY
}