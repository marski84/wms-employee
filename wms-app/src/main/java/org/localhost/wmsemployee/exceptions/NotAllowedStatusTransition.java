package org.localhost.wmsemployee.exceptions;

public class NotAllowedStatusTransition extends RuntimeException {
    public NotAllowedStatusTransition() {
        super("Not valid employee status transition");
    }
}
