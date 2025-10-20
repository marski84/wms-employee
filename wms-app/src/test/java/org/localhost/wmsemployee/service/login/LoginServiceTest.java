package org.localhost.wmsemployee.service.login;

import auth.dto.login.TokenResponseDto;
import auth.exceptions.AuthenticationFailedException;
import auth.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginService loginService;

    private final String testEmail = "test@example.com";
    private final String testPassword = "Password123!";
    private final String testAccessToken = "test-access-token";
    private final String testIdToken = "test-id-token";

    private TokenResponseDto testTokenResponse;

    @BeforeEach
    void setUp() {
        // Set configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(loginService, "audience", "https://test.auth0.com/api/v2/");
        ReflectionTestUtils.setField(loginService, "domain", "test.auth0.com");
        ReflectionTestUtils.setField(loginService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(loginService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(loginService, "auth0Connection", "Username-Password-Authentication");

        // Setup test token response
        testTokenResponse = new TokenResponseDto(testAccessToken, testIdToken, "Bearer", "86400");
    }

    // ========== handleApiLogin() Tests ==========

    @Test
    void handleApiLogin_shouldReturnTokenResponse_whenCredentialsAreValid() {
        // Arrange
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act
        TokenResponseDto result = loginService.handleApiLogin(testEmail, testPassword);

        // Assert
        assertNotNull(result);
        assertEquals(testAccessToken, result.getAccess_token());
        assertEquals(testIdToken, result.getId_token());
        assertEquals("Bearer", result.getToken_type());
        assertEquals("86400", result.getExpires_in());

        // Verify Auth0 was called
        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class));
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAuth0RejectsCredentials() {
        // Arrange
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.create(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldValidateCredentials_beforeCallingAuth0() {
        // Act & Assert - Invalid email format
        assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleApiLogin("not-an-email", testPassword)
        );

        verifyNoInteractions(restTemplate);
    }

    @Test
    void handleApiLogin_shouldValidatePasswordLength() {
        // Act & Assert - Password too short
        assertThrows(
                IllegalArgumentException.class,
                () -> loginService.handleApiLogin(testEmail, "short")
        );

        verifyNoInteractions(restTemplate);
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenTokenResponseIsNull() {
        // Arrange - Response body is null
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Failed to authenticate with Auth0", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAccessTokenIsNull() {
        // Arrange - Token response has null access_token
        TokenResponseDto invalidTokenResponse = new TokenResponseDto(null, testIdToken, "Bearer", "86400");
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(invalidTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("Failed to authenticate with Auth0", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        // Arrange
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loginService.handleApiLogin(testEmail, testPassword)
        );

        assertEquals("API login process failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Network error"));
    }
}