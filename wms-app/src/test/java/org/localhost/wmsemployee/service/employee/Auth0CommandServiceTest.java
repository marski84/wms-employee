package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import auth.dto.registration.Auth0UserCreationRequest;
import auth.error.Auth0ErrorCode;
import auth.exceptions.*;
import auth.service.Auth0ManagementTokenService;
import employee.dto.CreateUserDto;
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
    @Mock
    private Auth0RoleService auth0RoleService;
    private Auth0CommandService auth0CommandService;
    private CreateUserDto createUserDto;
    private Auth0RegistrationDto auth0Response;

    @BeforeEach
    void setUp() {
        auth0CommandService = new Auth0CommandService(restClient, auth0ManagementTokenService, auth0RoleService);

        // Set @Value fields using ReflectionTestUtils
        ReflectionTestUtils.setField(auth0CommandService, "auth0Domain", "test.auth0.com");
        ReflectionTestUtils.setField(auth0CommandService, "auth0UsersEndpoint", AUTH0_USERS_ENDPOINT);
        ReflectionTestUtils.setField(auth0CommandService, "auth0Connection", AUTH0_CONNECTION);

        // Mock Auth0RoleService to return a test role ID
        when(auth0RoleService.getRoleIdByName(anyString())).thenReturn("role_test123");

        // Mock RestClient for role assignment POST (used when role is provided in createUserDto)
        // This mocking is intentionally lenient as not all tests will trigger role assignment
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        createUserDto = new CreateUserDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Password@123",
                "Password@123",
                "Software Engineer",
                null,  // Role set to null to avoid triggering role assignment in basic tests
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
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
    @DisplayName("Should build correct DTO with user metadata")
    void testRegisterInAuth0_DtoStructure() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        // Capture the DTO
        ArgumentCaptor<Auth0UserCreationRequest> requestCaptor =
                ArgumentCaptor.forClass(Auth0UserCreationRequest.class);
        setupRestClientMocksWithCaptor(requestCaptor);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        auth0CommandService.registerInAuth0(createUserDto);

        // Then
        Auth0UserCreationRequest request = requestCaptor.getValue();
        assertThat(request.email()).isEqualTo("john.doe@example.com");
        assertThat(request.password()).isEqualTo("Password@123");
        assertThat(request.connection()).isEqualTo(AUTH0_CONNECTION);
        assertThat(request.emailVerified()).isFalse();
        assertThat(request.username()).isEqualTo("john.doe"); // extracted from email
        assertThat(request.nickname()).isEqualTo("John Doe"); // name + surname

        // Verify user metadata
        Auth0UserCreationRequest.UserMetadata metadata = request.userMetadata();
        assertThat(metadata.name()).isEqualTo("John");
        assertThat(metadata.surname()).isEqualTo("Doe");
        assertThat(metadata.phoneNumber()).isEqualTo("+48123456789");
        assertThat(metadata.jobTitle()).isEqualTo("Software Engineer");
        assertThat(metadata.role()).isNull(); // Role not set in this test (role assignment tested separately)
        assertThat(metadata.status()).isEqualTo("AVAILABLE");
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
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

        ArgumentCaptor<Auth0UserCreationRequest> requestCaptor =
                ArgumentCaptor.forClass(Auth0UserCreationRequest.class);
        setupRestClientMocksWithCaptor(requestCaptor);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        auth0CommandService.registerInAuth0(dtoWithNulls);

        // Then
        Auth0UserCreationRequest request = requestCaptor.getValue();
        Auth0UserCreationRequest.UserMetadata metadata = request.userMetadata();
        assertThat(metadata.role()).isNull();
        assertThat(metadata.status()).isNull();
    }

    @Test
    @DisplayName("Should throw Auth0UserAlreadyExistsException when Auth0 returns 409 Conflict")
    void testRegisterInAuth0_UserAlreadyExists() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT, "User already exists"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0UserAlreadyExistsException.class)
                .hasMessage(Auth0ErrorCode.USER_ALREADY_EXISTS.formatMessage("john.doe@example.com"))
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Password too weak"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ValidationException.class)
                .hasMessage(Auth0ErrorCode.VALIDATION_ERROR.getMessage())
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0PermissionException.class)
                .hasMessage(Auth0ErrorCode.PERMISSION_DENIED.getMessage())
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN, "Access denied"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0PermissionException.class)
                .hasMessage(Auth0ErrorCode.PERMISSION_DENIED.getMessage())
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ConnectionException.class)
                .hasMessage(Auth0ErrorCode.SERVICE_UNAVAILABLE.getMessage())
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ConnectionException.class)
                .hasMessage(Auth0ErrorCode.NETWORK_ERROR.getMessage())
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
        when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));

        // When & Then
        assertThatThrownBy(() -> auth0CommandService.registerInAuth0(createUserDto))
                .isInstanceOf(Auth0ServiceException.class)
                .hasMessageContaining("Unexpected error while creating Auth0 user")
                .hasCauseInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Should handle email without @ symbol defensively")
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
                null,  // No role to avoid triggering role assignment in this test
                EmployeeStatus.AVAILABLE,
                UUID.randomUUID()
        );

        when(auth0ManagementTokenService.getAccessToken()).thenReturn(MANAGEMENT_TOKEN);

        ArgumentCaptor<Auth0UserCreationRequest> requestCaptor =
                ArgumentCaptor.forClass(Auth0UserCreationRequest.class);
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(requestCaptor.capture())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(Auth0RegistrationDto.class)).thenReturn(auth0Response);

        // When
        auth0CommandService.registerInAuth0(malformedDto);

        // Then - should use full email as username instead of crashing
        Auth0UserCreationRequest request = requestCaptor.getValue();
        assertThat(request.username()).isEqualTo("invalid-email");
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
     * @param requestCaptor Optional captor for the request body. If null, uses any() matcher.
     * @param <T> Type of the request body being captured
     */
    private <T> void setupRestClientMocksWithCaptor(ArgumentCaptor<T> requestCaptor) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);

        if (requestCaptor != null) {
            when(requestBodySpec.body(requestCaptor.capture())).thenReturn(requestBodySpec);
        } else {
            when(requestBodySpec.body(any(Auth0UserCreationRequest.class))).thenReturn(requestBodySpec);
        }

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }
}