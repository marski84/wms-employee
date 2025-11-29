package employee.exception;

import java.util.UUID;

/**
 * Exception thrown when a department is not found.
 */
public class DepartmentNotFoundException extends RuntimeException {

    public DepartmentNotFoundException(UUID departmentId) {
        super("Department not found with ID: " + departmentId);
    }

    public DepartmentNotFoundException(String departmentName) {
        super("Department not found with name: " + departmentName);
    }
}