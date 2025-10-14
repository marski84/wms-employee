package org.localhost.wmsemployee.exceptions.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(ConstraintViolationException ex, HttpServletRequest request) {

        log.debug("validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();

        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        });

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
//    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(org.springframework.web.method.annotation.HandlerMethodValidationException ex, HttpServletRequest request) {
//
//        log.debug("Method parameter validation error: {}", ex.getMessage());
//
//        Map<String, String> errors = new HashMap<>();
//
//        ex.getParameterValidationResults().forEach(result -> result.getResolvableErrors().forEach(error -> {
//            String paramName = result.getMethodParameter().getParameterName();
//            String message = error.getDefaultMessage();
//            errors.put(paramName != null ? paramName : "param", message);
//        }));
//
//        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "Parameter validation failed", request.getRequestURI(), errors);
//
//        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String errorMessage;
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            Class<?> enumClass = ex.getRequiredType();
            errorMessage = String.format("Parameter '%s' must be one of the following values: %s", ex.getName(), Arrays.stream(enumClass.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", ")));
        } else {
            errorMessage = String.format("Parameter '%s' has invalid value: '%s'", ex.getName(), ex.getValue());
        }

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());

        log.debug("Type mismatch error: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAValidSupervisorException.class)
    public ResponseEntity<ErrorResponse> handleNotAValidSupervisorException(NotAValidSupervisorException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN, "You are not authorized to modify this employee's data", request.getRequestURI());

        log.warn("error while trying to update employee data by supervisor- not valid supervisor {}", errorResponse.getPath());

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFoundException(EmployeeNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse
                (HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NotValidTokenException.class)
    public ResponseEntity<ErrorResponse> handleNotValidTokenException(NotValidTokenException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse
                (HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordNotValidException.class)
    public ResponseEntity<ErrorResponse> handleNotValidTokenException(PasswordNotValidException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse
                (HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotAllowedStatusTransition.class)
    public ResponseEntity<ErrorResponse> handleNotAllowedStatusTransition(NotAllowedStatusTransition ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse
                (HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailedException(AuthenticationFailedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());

        log.warn("Authentication failed: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());

        log.warn("User not found: {}", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}
