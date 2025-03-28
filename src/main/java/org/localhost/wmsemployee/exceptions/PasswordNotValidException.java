package org.localhost.wmsemployee.exceptions;

public class PasswordNotValidException extends RuntimeException {
    public PasswordNotValidException() {
        super("Password is not valid");
    }
}
