package employee.exception;

/**
 * Standardized error codes for API responses.
 * Provides client-safe error identifiers without exposing technical details.
 */
public enum ErrorCode {
    // Resource not found errors
    USER_NOT_FOUND("User not found"),
    DEPARTMENT_NOT_FOUND("Department not found"),

    // Conflict errors
    EMAIL_ALREADY_EXISTS("Email address is already registered"),
    DEPARTMENT_NAME_ALREADY_EXISTS("Department name already exists"),

    // Validation errors
    VALIDATION_ERROR("Invalid request data"),

    // Server errors
    INTERNAL_ERROR("An unexpected error occurred. Please contact support if the problem persists");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}