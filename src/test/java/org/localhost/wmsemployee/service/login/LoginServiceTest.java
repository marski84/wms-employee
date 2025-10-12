package org.localhost.wmsemployee.service.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.TokenResponseDto;
import org.localhost.wmsemployee.exceptions.AuthenticationFailedException;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.localhost.wmsemployee.service.employee.EmployeeDataService;
import org.localhost.wmsemployee.service.employee.EmployeeQueryService;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private EmployeeDataService employeeDataService;

    @Mock
    private Auth0ManagementTokenService auth0ManagementTokenService;

    @Mock
    private EmployeeQueryService employeeQueryService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginService loginService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "Password123!";
    private final String testUserId = "auth0|123456";
    private final String testAccessToken = "test-access-token";
    private final String testIdToken = "test-id-token";

    private EmployeeData testEmployee;
    private Auth0UserDto testUserDto;
    private TokenResponseDto testTokenResponse;

    @BeforeEach
    void setUp() {
        // Set configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(loginService, "audience", "https://test.auth0.com/api/v2/");
        ReflectionTestUtils.setField(loginService, "domain", "test.auth0.com");
        ReflectionTestUtils.setField(loginService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(loginService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(loginService, "auth0Connection", "Username-Password-Authentication");

        // Setup test employee
        testEmployee = EmployeeData.builder()
                .id(1L)
                .username("testuser")
                .userId(testUserId)
                .email(testEmail)
                .build();

        // Setup test user DTO
        testUserDto = Auth0UserDto.builder()
                .userId(testUserId)
                .email(testEmail)
                .name("Test User")
                .username("testuser")
                .build();

        // Setup test token response
        testTokenResponse = new TokenResponseDto(testAccessToken, testIdToken, "Bearer", "86400");
    }

    // ========== handleLogin() Tests ==========

    @Test
    void handleLogin_shouldReturnUserDetails_whenCredentialsAreValid() {
        // Given
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeQueryService.getEmployeeDetailsByUserId(testUserId, testAccessToken))
                .thenReturn(testUserDto);

        // When
        Auth0UserDto result = loginService.handleLogin(testEmail, testPassword);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(testEmail, result.getEmail());

        // Verify the order: Auth0 authentication happens BEFORE database check
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class));
        verify(employeeDataService).findByEmail(testEmail);
        verify(employeeQueryService).getEmployeeDetailsByUserId(testUserId, testAccessToken);
    }

    @Test
    void handleLogin_shouldThrowAuthenticationFailed_whenAuth0RejectsCredentials() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.create(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleLogin(testEmail, testPassword)
        );

        assertEquals("Invalid credentials", exception.getMessage());

        // Verify database was never checked (fail fast at Auth0)
        verify(employeeDataService, never()).findByEmail(anyString());
        verify(employeeQueryService, never()).getEmployeeDetailsByUserId(anyString(), anyString());
    }

    @Test
    void handleLogin_shouldThrowAuthenticationFailed_whenUserNotFoundInDatabase() {
        // Given - Auth0 authentication succeeds
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // But user doesn't exist in our database
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleLogin(testEmail, testPassword)
        );

        // Critical security check: same error message as invalid credentials
        assertEquals("Invalid credentials", exception.getMessage());

        // Verify Auth0 was called first
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class));
        verify(employeeDataService).findByEmail(testEmail);
        verify(employeeQueryService, never()).getEmployeeDetailsByUserId(anyString(), anyString());
    }

    @Test
    void handleLogin_shouldThrowIllegalArgument_whenEmailIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleLogin(null, testPassword)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
        verifyNoInteractions(restTemplate, employeeDataService, employeeQueryService);
    }

    @Test
    void handleLogin_shouldThrowIllegalArgument_whenEmailIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleLogin("", testPassword)
        );

        assertEquals("Email cannot be null or empty", exception.getMessage());
        verifyNoInteractions(restTemplate, employeeDataService, employeeQueryService);
    }

    @Test
    void handleLogin_shouldThrowIllegalArgument_whenEmailFormatIsInvalid() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleLogin("invalid-email", testPassword)
        );

        assertEquals("Invalid email format", exception.getMessage());
        verifyNoInteractions(restTemplate, employeeDataService, employeeQueryService);
    }

    @Test
    void handleLogin_shouldThrowIllegalArgument_whenPasswordIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleLogin(testEmail, null)
        );

        assertTrue(exception.getMessage().contains("Password must be at least"));
        verifyNoInteractions(restTemplate, employeeDataService, employeeQueryService);
    }

    @Test
    void handleLogin_shouldThrowIllegalArgument_whenPasswordIsTooShort() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleLogin(testEmail, "12345") // Only 5 chars
        );

        assertTrue(exception.getMessage().contains("Password must be at least 6 characters long"));
        verifyNoInteractions(restTemplate, employeeDataService, employeeQueryService);
    }

    @Test
    void handleLogin_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loginService.handleLogin(testEmail, testPassword)
        );

        assertEquals("Login process failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Unexpected error"));
    }

    // ========== handleApiLogin() Tests ==========

    @Test
    void handleApiLogin_shouldReturnTokenResponse_whenCredentialsAreValid() {
        // Given
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.of(testEmployee));

        // When
        TokenResponseDto result = loginService.handleApiLogin(testEmail, testPassword);

        // Then
        assertNotNull(result);
        assertEquals(testAccessToken, result.getAccess_token());
        assertEquals(testIdToken, result.getId_token());
        assertEquals("Bearer", result.getToken_type());
        assertEquals("86400", result.getExpires_in());

        // Verify the order: Auth0 first, then database check
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class));
        verify(employeeDataService).findByEmail(testEmail);
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAuth0RejectsCredentials() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.create(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(employeeDataService, never()).findByEmail(anyString());
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenUserNotFoundInDatabase() {
        // Given
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        // User enumeration prevention: same error message
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldValidateCredentials_beforeCallingAuth0() {
        // When & Then - Invalid email format
        assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleApiLogin("not-an-email", testPassword)
        );

        verifyNoInteractions(restTemplate);
    }

    // ========== Authentication Request Tests ==========

    @Test
    void handleLogin_shouldSendCorrectAuthenticationRequestToAuth0() {
        // Given
        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), requestCaptor.capture(), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeQueryService.getEmployeeDetailsByUserId(anyString(), anyString()))
                .thenReturn(testUserDto);

        // When
        loginService.handleLogin(testEmail, testPassword);

        // Then - Verify request structure
        HttpEntity<Map<String, Object>> capturedRequest = requestCaptor.getValue();
        Map<String, Object> requestBody = capturedRequest.getBody();

        assertNotNull(requestBody);
        assertEquals("password", requestBody.get("grant_type"));
        assertEquals(testEmail, requestBody.get("username"));
        assertEquals(testPassword, requestBody.get("password"));
        assertEquals("test-client-id", requestBody.get("client_id"));
        assertEquals("test-client-secret", requestBody.get("client_secret"));
        assertEquals("https://test.auth0.com/api/v2/", requestBody.get("audience"));
        assertEquals("openid profile email", requestBody.get("scope"));
        assertEquals("Username-Password-Authentication", requestBody.get("connection"));
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenTokenResponseIsNull() {
        // Given - Response body is null
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Failed to authenticate with Auth0", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAccessTokenIsNull() {
        // Given - Token response has null access_token
        TokenResponseDto invalidTokenResponse = new TokenResponseDto(null, testIdToken, "Bearer", "86400");
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(invalidTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // When & Then
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Failed to authenticate with Auth0", exception.getMessage());
    }

    // ========== Edge Cases ==========

    @Test
    void handleLogin_shouldHandleAuth0DomainWithWhitespace() {
        // Given - Domain has extra whitespace
        ReflectionTestUtils.setField(loginService, "domain", "  test.auth0.com  ");

        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(contains("https://test.auth0.com/oauth/token"),
                any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);
        when(employeeDataService.findByEmail(testEmail)).thenReturn(Optional.of(testEmployee));
        when(employeeQueryService.getEmployeeDetailsByUserId(anyString(), anyString()))
                .thenReturn(testUserDto);

        // When
        Auth0UserDto result = loginService.handleLogin(testEmail, testPassword);

        // Then
        assertNotNull(result);
        // Verify the domain whitespace was cleaned
        verify(restTemplate).postForEntity(
                eq("https://test.auth0.com/oauth/token"),
                any(HttpEntity.class),
                eq(TokenResponseDto.class)
        );
    }

    @Test
    void handleApiLogin_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        // Given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("API login process failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Network error"));
    }
}