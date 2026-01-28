package auth.exceptions;

/**
 * Base exception for all Auth0-related service errors.
 * Provides a parent class for specific Auth0 error scenarios.
 */
public class Auth0ServiceException extends RuntimeException {

    public Auth0ServiceException(String message) {
        super(message);
    }

    public Auth0ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}