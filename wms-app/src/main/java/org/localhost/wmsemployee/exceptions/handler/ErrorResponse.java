package org.localhost.wmsemployee.exceptions.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
public class ErrorResponse {
    private final ZonedDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private Map<String, String> validationErrors;

    public ErrorResponse(HttpStatus status, String message, String path) {
        this.timestamp = ZonedDateTime.now();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(HttpStatus status, String message, String path, Map<String, String> validationErrors) {
        this(status, message, path);
        this.validationErrors = validationErrors;
    }
}

