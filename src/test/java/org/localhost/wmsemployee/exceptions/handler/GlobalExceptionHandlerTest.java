package org.localhost.wmsemployee.exceptions.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.exceptions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    private final String testPath = "/api/test";

    @BeforeEach
    void setUp() {
        when(request.getRequestURI()).thenReturn(testPath);
    }

    // ========== Authentication Exceptions ==========

    @Test
    void handleAuthenticationFailedException_shouldReturn401() {
        // Given
        AuthenticationFailedException exception = new AuthenticationFailedException("Invalid credentials");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals(testPath, response.getBody().getPath());
    }

    @Test
    void handleAuthenticationFailedException_shouldHaveTimestamp() {
        // Given
        AuthenticationFailedException exception = new AuthenticationFailedException("User not authenticated");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        // Timestamp should be recent (within last minute)
        assertTrue(errorResponse.getTimestamp().isAfter(
                java.time.ZonedDateTime.now().minusMinutes(1)));
    }

    // ========== Not Found Exceptions ==========

    @Test
    void handleUserNotFoundException_shouldReturn404() {
        // Given
        UserNotFoundException exception = new UserNotFoundException("User with ID 123 not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFoundException(exception, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User with ID 123 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
    }

    @Test
    void handleEmployeeNotFoundException_shouldReturn404() {
        // Given
        EmployeeNotFoundException exception = new EmployeeNotFoundException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEmployeeNotFoundException(exception, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Employee not found!", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    // ========== Validation Exceptions ==========

    @Test
    void handleValidationExceptions_shouldReturn400() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("must be a valid email address");

        ConstraintViolationException exception = new ConstraintViolationException(
                "Validation failed", Set.of(violation));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptions_shouldIncludeFieldErrors() {
        // Given
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path propertyPath1 = mock(Path.class);
        when(violation1.getPropertyPath()).thenReturn(propertyPath1);
        when(propertyPath1.toString()).thenReturn("email");
        when(violation1.getMessage()).thenReturn("must be a valid email address");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path propertyPath2 = mock(Path.class);
        when(violation2.getPropertyPath()).thenReturn(propertyPath2);
        when(propertyPath2.toString()).thenReturn("password");
        when(violation2.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException exception = new ConstraintViolationException(
                "Validation failed", Set.of(violation1, violation2));

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        Map<String, String> validationErrors = errorResponse.getValidationErrors();
        assertNotNull(validationErrors);
        assertEquals(2, validationErrors.size());
        assertTrue(validationErrors.containsKey("email"));
        assertTrue(validationErrors.containsKey("password"));
        assertEquals("must be a valid email address", validationErrors.get("email"));
        assertEquals("must not be blank", validationErrors.get("password"));
    }

    @Test
    void handleMethodArgumentTypeMismatch_shouldReturn400() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        when(exception.getName()).thenReturn("userId");
        when(exception.getValue()).thenReturn("abc");
        when(exception.getRequiredType()).thenReturn((Class) Long.class);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatch(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("userId"));
        assertTrue(response.getBody().getMessage().contains("abc"));
    }

    @Test
    void handleMethodArgumentTypeMismatch_shouldHandleEnumType() {
        // Given
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class, withSettings().lenient());
        when(exception.getName()).thenReturn("status");
        when(exception.getValue()).thenReturn("INVALID_STATUS");
        when(exception.getRequiredType()).thenReturn((Class) HttpStatus.class);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentTypeMismatch(exception, request);

        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        String message = errorResponse.getMessage();
        // Should mention it's an enum and show valid values
        assertTrue(message.contains("status"));
        assertTrue(message.contains("one of the following values") || message.contains("must be"));
    }

    // ========== Business Logic Exceptions ==========

    @Test
    void handleNotAValidSupervisorException_shouldReturn403() {
        // Given
        NotAValidSupervisorException exception = new NotAValidSupervisorException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotAValidSupervisorException(exception, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("You are not authorized to modify this employee's data", response.getBody().getMessage());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
    }

    @Test
    void handleNotValidTokenException_shouldReturn400() {
        // Given
        NotValidTokenException exception = new NotValidTokenException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotValidTokenException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handlePasswordNotValidException_shouldReturn400() {
        // Given
        PasswordNotValidException exception = new PasswordNotValidException();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotValidTokenException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleNotAllowedStatusTransition_shouldReturn400() {
        // Given
        NotAllowedStatusTransition exception = new NotAllowedStatusTransition();

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNotAllowedStatusTransition(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    // ========== ErrorResponse Consistency Tests ==========

    @Test
    void allExceptionHandlers_shouldReturnConsistentErrorResponseStructure() {
        // Given - various exceptions
        AuthenticationFailedException authEx = new AuthenticationFailedException("Auth failed");
        UserNotFoundException notFoundEx = new UserNotFoundException("User not found");
        NotValidTokenException validationEx = new NotValidTokenException();

        // When
        ResponseEntity<ErrorResponse> authResponse = exceptionHandler.handleAuthenticationFailedException(authEx, request);
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleUserNotFoundException(notFoundEx, request);
        ResponseEntity<ErrorResponse> validationResponse = exceptionHandler.handleNotValidTokenException(validationEx, request);

        // Then - All should have consistent structure
        assertNotNull(authResponse.getBody());
        assertNotNull(authResponse.getBody().getTimestamp());
        assertNotNull(authResponse.getBody().getMessage());
        assertNotNull(authResponse.getBody().getError());
        assertNotNull(authResponse.getBody().getPath());
        assertTrue(authResponse.getBody().getStatus() > 0);

        assertNotNull(notFoundResponse.getBody());
        assertNotNull(notFoundResponse.getBody().getTimestamp());
        assertNotNull(notFoundResponse.getBody().getMessage());
        assertTrue(notFoundResponse.getBody().getStatus() > 0);

        assertNotNull(validationResponse.getBody());
        assertNotNull(validationResponse.getBody().getTimestamp());
        assertNotNull(validationResponse.getBody().getMessage());
        assertTrue(validationResponse.getBody().getStatus() > 0);
    }

    @Test
    void errorResponse_shouldMatchHttpStatus() {
        // Given
        AuthenticationFailedException authEx = new AuthenticationFailedException("Auth failed");
        UserNotFoundException notFoundEx = new UserNotFoundException("Not found");
        NotValidTokenException badRequestEx = new NotValidTokenException();
        NotAValidSupervisorException forbiddenEx = new NotAValidSupervisorException();

        // When & Then - Status code in body should match HTTP status
        ResponseEntity<ErrorResponse> authResponse = exceptionHandler.handleAuthenticationFailedException(authEx, request);
        assertEquals(authResponse.getStatusCode().value(), authResponse.getBody().getStatus());

        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleUserNotFoundException(notFoundEx, request);
        assertEquals(notFoundResponse.getStatusCode().value(), notFoundResponse.getBody().getStatus());

        ResponseEntity<ErrorResponse> badRequestResponse = exceptionHandler.handleNotValidTokenException(badRequestEx, request);
        assertEquals(badRequestResponse.getStatusCode().value(), badRequestResponse.getBody().getStatus());

        ResponseEntity<ErrorResponse> forbiddenResponse = exceptionHandler.handleNotAValidSupervisorException(forbiddenEx, request);
        assertEquals(forbiddenResponse.getStatusCode().value(), forbiddenResponse.getBody().getStatus());
    }

    @Test
    void errorResponse_shouldIncludeRequestPath() {
        // Given
        String customPath = "/api/custom/path";
        when(request.getRequestURI()).thenReturn(customPath);
        AuthenticationFailedException exception = new AuthenticationFailedException("Test");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationFailedException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(customPath, response.getBody().getPath());
    }

    @Test
    void errorResponse_shouldHaveHttpStatusReasonPhrase() {
        // Given
        AuthenticationFailedException authEx = new AuthenticationFailedException("Auth failed");
        EmployeeNotFoundException notFoundEx = new EmployeeNotFoundException();
        NotValidTokenException badRequestEx = new NotValidTokenException();

        // When
        ResponseEntity<ErrorResponse> authResponse = exceptionHandler.handleAuthenticationFailedException(authEx, request);
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleEmployeeNotFoundException(notFoundEx, request);
        ResponseEntity<ErrorResponse> badRequestResponse = exceptionHandler.handleNotValidTokenException(badRequestEx, request);

        // Then - Should have HTTP status reason phrases
        assertEquals("Unauthorized", authResponse.getBody().getError());
        assertEquals("Not Found", notFoundResponse.getBody().getError());
        assertEquals("Bad Request", badRequestResponse.getBody().getError());
    }
}