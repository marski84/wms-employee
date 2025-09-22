package org.localhost.wmsemployee.exceptions;

public class NoValidDtoException extends RuntimeException {
    public NoValidDtoException() {
        super("No valid dto provided");
    }
}
