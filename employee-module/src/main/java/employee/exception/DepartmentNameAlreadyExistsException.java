package employee.exception;

/**
 * Exception thrown when attempting to create a department with a name that already exists.
 */
public class DepartmentNameAlreadyExistsException extends RuntimeException {

    public DepartmentNameAlreadyExistsException(String departmentName) {
        super("Department with name already exists: " + departmentName);
    }
}