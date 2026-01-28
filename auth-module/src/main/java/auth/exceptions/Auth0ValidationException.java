package auth.exceptions;

/**
 * Exception thrown when Auth0 rejects the request due to validation errors.
 * Common scenarios:
 * - Password doesn't meet complexity requirements
 * - Invalid email format
 * - Missing required fields
 * - Invalid user metadata structure
 *
 * Typically corresponds to HTTP 400 Bad Request response from Auth0 Management API.
 */
public class Auth0ValidationException extends Auth0ServiceException {

    public Auth0ValidationException(String message) {
        super(message);
    }

    public Auth0ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}