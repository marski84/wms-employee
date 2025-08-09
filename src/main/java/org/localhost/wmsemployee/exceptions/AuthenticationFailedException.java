package org.localhost.wmsemployee.exceptions;

/**
 * Exception thrown when authentication fails due to invalid credentials
 * or other authentication-related issues.
 */
public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}