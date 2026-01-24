package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import auth.exceptions.*;
import auth.service.Auth0ManagementTokenService;
import employee.dto.CreateUserDto;
import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Auth0CommandService.
 * Tests Auth0 user registration via Management API.
 * Uses LENIENT strictness due to RestClient fluent API mocking complexity.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Auth0CommandService Unit Tests")
class Auth0CommandServiceTest {

    private static final String AUTH0_USER_ID = "auth0|abc123def456";
    private static final String MANAGEMENT_TOKEN = "test-management-token";
    private static final String AUTH0_USERS_ENDPOINT = "https://test.auth0.com/api/v2/users";
    private static final String AUTH0_CONNECTION = "Username-Password-Authentication";
    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;
    @Mock
    private Auth0ManagementTokenService auth0ManagementTokenService;
    private Auth0CommandService auth0CommandService;
    private CreateUserDto createUserDto;
    private Auth0RegistrationDto auth0Response;

    @BeforeEach
    void setUp() {
        auth0CommandService = new Auth0CommandService(restClient, auth0ManagementTokenService);

        // Set @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(auth0CommandService, "auth0Domain", "test.auth0.com");
        ReflectionTestUtils.setField(auth0CommandService, "auth0UsersEndpoint", AUTH0_USERS_ENDPOINT);
        ReflectionTestUtils.setField(auth0CommandService, "auth0Connection", AUTH0_CONNECTION);

        createUserDto = new CreateUserDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Password@123",
                "Password@123",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                UUID.randomUUID()
        );

        auth0Response = new Auth0RegistrationDto(
                AUTH0_USER_ID,
                "john.doe@example.com",
                "John Doe",
                "john.doe",
                "2026-01-18T12:00:00.000Z"
        );
    }

    @Test
    @DisplayName("Should register user in Auth0 successfully")
    void testRegisterInAuth0_Success() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        // Inline mock setup to avoid interference
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(AUTH0_USERS_ENDPOINT)).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        Auth0RegistrationDto result = auth0CommandService.registerInAuth0(createUserDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(AUTH0_USER_ID);
        assertThat(result.email()).isEqualTo("john.doe@example.com");

        // Verify token was requested
        verify(auth0ManagementTokenService).getAccessToken();

        // Verify RestClient was called with correct endpoint
        verify(restClient).post();
        verify(requestBodyUriSpec).uri(AUTH0_USERS_ENDPOINT);
    }

    @Test
    @DisplayName("Should build correct payload with user metadata")
    @SuppressWarnings("unchecked")
    void testRegisterInAuth0_PayloadStructure() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        // Capture the payload
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        setupRestClientMocksWithCaptor(payloadCaptor);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        auth0CommandService.registerInAuth0(createUserDto);

        // Then
        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload).containsEntry("email", "john.doe@example.com");
        assertThat(payload).containsEntry("password", "Password@123");
        assertThat(payload).containsEntry("connection", AUTH0_CONNECTION);
        assertThat(payload).containsEntry("email_verified", false);
        assertThat(payload).containsEntry("username", "john.doe"); // extracted from email
        assertThat(payload).containsEntry("nickname", "John Doe"); // name + surname

        // Verify user_metadata
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) payload.get("user_metadata");
        assertThat(metadata).containsEntry("name", "John");
        assertThat(metadata).containsEntry("surname", "Doe");
        assertThat(metadata).containsEntry("phoneNumber", "+48123456789");
        assertThat(metadata).containsEntry("jobTitle", "Software Engineer");
        assertThat(metadata).containsEntry("role", "EMPLOYEE");
        assertThat(metadata).containsEntry("status", "AVAILABLE");
    }

    @Test
    @DisplayName("Should propagate exception when token service fails")
    void testRegisterInAuth0_TokenServiceFailure() {
        // Given
        when(auth0ManagementTokenService.getAccessToken())
                .thenThrow(new RuntimeException("Failed to get management token"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to get management token");

        // Verify RestClient was never called
        verify(restClient, never()).post();
    }

    @Test
    @DisplayName("Should propagate exception when Auth0 API returns error")
    void testRegisterInAuth0_Auth0ApiError() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        // Inline mock setup
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new RuntimeException("Auth0 API returned 409 Conflict"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Auth0 API returned 409 Conflict");
    }

    @Test
    @DisplayName("Should handle null role and status gracefully")
    @SuppressWarnings("unchecked")
    void testRegisterInAuth0_NullRoleAndStatus() {
        // Given
        CreateUserDto dtoWithNulls = new CreateUserDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Password@123",
                "Password@123",
                "Software Engineer",
                null, // role is null
                null, // status is null
                null
        );

        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);
        setupRestClientMocks();
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        when(requestBodySpec.body(payloadCaptor.capture())).thenReturn(requestBodySpec);

        // When
        auth0CommandService.registerInAuth0(dtoWithNulls);

        // Then
        Map<String, Object> payload = payloadCaptor.getValue();
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) payload.get("user_metadata");
        assertThat(metadata.get("role")).isNull();
        assertThat(metadata.get("status")).isNull();
    }

    @Test
    @DisplayName("Should throw Auth0UserAlreadyExistsException when Auth0 returns 409 Conflict")
    void testRegisterInAuth0_UserAlreadyExists() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "User already exists"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0UserAlreadyExistsException.class)
                .hasMessageContaining("john.doe@example.com")
                .hasMessageContaining("already exists")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should throw Auth0ValidationException when Auth0 returns 400 Bad Request")
    void testRegisterInAuth0_ValidationError() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Password too weak"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ValidationException.class)
                .hasMessageContaining("Invalid data or password requirements not met")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should throw Auth0PermissionException when Auth0 returns 401 Unauthorized")
    void testRegisterInAuth0_Unauthorized() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0PermissionException.class)
                .hasMessageContaining("Insufficient permissions")
                .hasMessageContaining("M2M token scopes")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should throw Auth0PermissionException when Auth0 returns 403 Forbidden")
    void testRegisterInAuth0_Forbidden() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access denied"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0PermissionException.class)
                .hasMessageContaining("Insufficient permissions")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should throw Auth0ConnectionException when Auth0 returns 5xx error")
    void testRegisterInAuth0_ServerError() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ConnectionException.class)
                .hasMessageContaining("temporarily unavailable")
                .hasCauseInstanceOf(HttpServerErrorException.class);
    }

    @Test
    @DisplayName("Should throw Auth0ConnectionException on network timeout")
    void testRegisterInAuth0_NetworkTimeout() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ConnectionException.class)
                .hasMessageContaining("Cannot reach Auth0 service")
                .hasMessageContaining("network connectivity")
                .hasCauseInstanceOf(ResourceAccessException.class);
    }

    @Test
    @DisplayName("Should throw Auth0ServiceException for other HTTP client errors")
    void testRegisterInAuth0_UnexpectedClientError() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ServiceException.class)
                .hasMessageContaining("Unexpected error")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should handle email without @ symbol defensively")
    @SuppressWarnings("unchecked")
    void testRegisterInAuth0_EmailWithoutAtSymbol() {
        // Given - malformed email (shouldn't happen with @Email validation, but defensive)
        CreateUserDto malformedDto = new CreateUserDto(
                "John",
                "Doe",
                "invalid-email",  // No @ symbol
                "+48123456789",
                "Password@123",
                "Password@123",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                UUID.randomUUID()
        );

        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(payloadCaptor.capture())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        auth0CommandService.registerInAuth0(malformedDto);

        // Then - should use full email as username instead of crashing
        Map<String, Object> payload = payloadCaptor.getValue();
        assertThat(payload.get("username")).isEqualTo("invalid-email");
    }

    /**
     * Helper method to setup RestClient mock chain.
     */
    private void setupRestClientMocks() {
        setupRestClientMocksWithCaptor(null);
    }

    /**
     * Helper method to setup RestClient mock chain with optional ArgumentCaptor for body.
     *
     * @param payloadCaptor Optional captor for the request body. If null, uses any() matcher.
     */
    @SuppressWarnings("unchecked")
    private void setupRestClientMocksWithCaptor(ArgumentCaptor<Map<String, Object>> payloadCaptor) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);

        if (payloadCaptor != null) {
            when(requestBodySpec.body(payloadCaptor.capture())).thenReturn(requestBodySpec);
        } else {
            when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
        }

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }
}