package org.localhost.wmsemployee.exceptions;

public class PasswordDoesNotMatchConfirmPasswordException extends RuntimeException {

    public PasswordDoesNotMatchConfirmPasswordException() {
        super("Password does not match confirm password");
    }
}
