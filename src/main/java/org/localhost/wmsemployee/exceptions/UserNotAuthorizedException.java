package org.localhost.wmsemployee.exceptions;

public class UserNotAuthorizedException extends RuntimeException {

    public UserNotAuthorizedException() {
        super("User not authorized");
    }
}
