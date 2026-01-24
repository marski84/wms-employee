package auth.exceptions;

/**
 * Exception thrown when the Auth0 Management API token lacks required permissions.
 * Common scenarios:
 * - M2M application missing required scopes (create:users, update:users, etc.)
 * - Invalid or expired management token
 * - Token not authorized for the requested operation
 * <p>
 * Typically corresponds to HTTP 401 Unauthorized or 403 Forbidden from Auth0.
 */
public class Auth0PermissionException extends Auth0ServiceException {

    public Auth0PermissionException(String message) {
        super(message);
    }

    public Auth0PermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}