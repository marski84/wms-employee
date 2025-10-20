package org.localhost.wmsemployee.exceptions;

public class NotAValidSupervisorException extends RuntimeException {
    public NotAValidSupervisorException() {
        super("Supervisor is not valid");
    }
}
