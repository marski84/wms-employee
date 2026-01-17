package employee.exception.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import employee.exception.ErrorCode;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Standardized error response structure for API errors.
 * Provides consistent error format without exposing sensitive technical details.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final ZonedDateTime timestamp;
    private final int status;
    private final String errorCode;
    private final String message;
    private final String path;
    private final Map<String, String> validationErrors;

    public ErrorResponse(int status, ErrorCode errorCode, String path) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.errorCode = errorCode.name();
        this.message = errorCode.getDefaultMessage();
        this.path = path;
        this.validationErrors = null;
    }

    public ErrorResponse(int status, ErrorCode errorCode, String path, Map<String, String> validationErrors) {
        this.timestamp = ZonedDateTime.now();
        this.status = status;
        this.errorCode = errorCode.name();
        this.message = errorCode.getDefaultMessage();
        this.path = path;
        this.validationErrors = validationErrors;
    }
}