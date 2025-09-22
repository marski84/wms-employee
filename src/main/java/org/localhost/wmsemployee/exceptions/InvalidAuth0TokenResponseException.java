package org.localhost.wmsemployee.exceptions;

/**
 * Exception thrown when the Auth0 token response is invalid or missing required fields.
 */
public class InvalidAuth0TokenResponseException extends RuntimeException {

    public InvalidAuth0TokenResponseException(String message) {
        super(message);
    }

    public InvalidAuth0TokenResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}