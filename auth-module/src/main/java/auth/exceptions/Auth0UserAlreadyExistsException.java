package auth.exceptions;

/**
 * Exception thrown when attempting to create a user in Auth0 that already exists.
 * Typically corresponds to HTTP 409 Conflict response from Auth0 Management API.
 */
public class Auth0UserAlreadyExistsException extends Auth0ServiceException {

    public Auth0UserAlreadyExistsException(String message) {
        super(message);
    }

    public Auth0UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}