package employee.exception.handler;

import employee.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for employee module.
 * Centralizes exception handling and provides consistent error responses.
 */
//@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException.
     * Returns 404 NOT FOUND status with sanitized error message.
     * Technical details are logged server-side only.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request) {
        log.error("User not found - Details: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.USER_NOT_FOUND,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles DepartmentNotFoundException.
     * Returns 404 NOT FOUND status with sanitized error message.
     * Technical details are logged server-side only.
     */
    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFoundException(
            DepartmentNotFoundException ex,
            HttpServletRequest request) {
        log.error("Department not found - Details: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ErrorCode.DEPARTMENT_NOT_FOUND,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handles EmailAlreadyExistsException.
     * Returns 409 CONFLICT status with sanitized error message.
     * Technical details are logged server-side only.
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {
        log.error("Email already exists - Details: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ErrorCode.EMAIL_ALREADY_EXISTS,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles DepartmentNameAlreadyExistsException.
     * Returns 409 CONFLICT status with sanitized error message.
     * Technical details are logged server-side only.
     */
    @ExceptionHandler(DepartmentNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNameAlreadyExistsException(
            DepartmentNameAlreadyExistsException ex,
            HttpServletRequest request) {
        log.error("Department name already exists - Details: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ErrorCode.DEPARTMENT_NAME_ALREADY_EXISTS,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handles Bean Validation errors from @Valid annotation.
     * Returns 400 BAD REQUEST status with field-level validation errors.
     * Field names are included for UX purposes (client needs to know which fields to fix).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.warn("Validation failed - Fields: {}",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getField)
                        .toList());

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ErrorCode.VALIDATION_ERROR,
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     * Returns 500 INTERNAL SERVER ERROR status with generic message.
     * Full stack trace and details are logged server-side only - NEVER sent to client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred - Type: {}, Message: {}",
                ex.getClass().getName(), ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ErrorCode.INTERNAL_ERROR,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}