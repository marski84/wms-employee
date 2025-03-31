package org.localhost.wmsemployee.exceptions;

public class NotValidTokenException extends RuntimeException {
    public NotValidTokenException() {
        super("Invalid token");
    }
}
