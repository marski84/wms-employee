package auth.exceptions;

/**
 * Exception thrown when unable to connect to Auth0 service.
 * Common scenarios:
 * - Network timeout
 * - Connection refused
 * - DNS resolution failure
 * - Auth0 service temporarily unavailable (5xx errors)
 * <p>
 * This is typically a transient error that may succeed on retry.
 */
public class Auth0ConnectionException extends Auth0ServiceException {

    public Auth0ConnectionException(String message) {
        super(message);
    }

    public Auth0ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}