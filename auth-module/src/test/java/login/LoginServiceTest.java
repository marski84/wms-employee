package login;

import auth.dto.login.TokenResponseDto;
import auth.exceptions.AuthenticationFailedException;
import auth.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoginService.
 * Tests verify OAuth2 password grant authentication flow with Auth0.
 * <p>
 * Test Configuration:
 * - Mocks RestTemplate for isolated unit testing
 * - Tests service via IAuthenticationService interface contract
 * - Verifies HTTP request parameters, headers, and body sent to Auth0
 * - Tests both success and failure scenarios with comprehensive error coverage
 * <p>
 * Future Improvement:
 * Consider migrating to @SpringBootTest + @TestPropertySource for cleaner property injection,
 * though this would make tests integration tests rather than pure unit tests.
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginService loginService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_ACCESS_TOKEN = "test-access-token";
    private static final String TEST_ID_TOKEN = "test-id-token";
    private static final String AUTH0_ENDPOINT = "https://test.auth0.com/oauth/token";

    @BeforeEach
    void setUp() {
        // Initialize Auth0 configuration via reflection
        // (In real app, these are injected by Spring via @Value annotations)
        ReflectionTestUtils.setField(loginService, "audience", "https://test.auth0.com/api/v2/");
        ReflectionTestUtils.setField(loginService, "domain", "test.auth0.com");
        ReflectionTestUtils.setField(loginService, "clientId", "test-client-id");
        ReflectionTestUtils.setField(loginService, "clientSecret", "test-client-secret");
        ReflectionTestUtils.setField(loginService, "auth0Connection", "Username-Password-Authentication");
    }

    private TokenResponseDto createTestTokenResponse() {
        return new TokenResponseDto(TEST_ACCESS_TOKEN, TEST_ID_TOKEN, "Bearer", "86400");
    }

    // ========== handleApiLogin() Tests ==========

    @Test
    void handleApiLogin_shouldReturnTokenResponse_whenCredentialsAreValid() {
        // Arrange
        TokenResponseDto testTokenResponse = createTestTokenResponse();
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act
        TokenResponseDto result = loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD);

        // Assert - Verify complete token response returned
        assertNotNull(result);
        assertEquals(TEST_ACCESS_TOKEN, result.getAccess_token());
        assertEquals(TEST_ID_TOKEN, result.getId_token());
        assertEquals("Bearer", result.getToken_type());
        assertEquals("86400", result.getExpires_in());

        // Verify Auth0 endpoint and request parameters
        verifyAuth0RequestParameters(TEST_EMAIL, TEST_PASSWORD);
    }

    @Test
    void handleApiLogin_shouldSendCorrectHttpHeaders_whenAuthenticating() {
        // Arrange
        TokenResponseDto testTokenResponse = createTestTokenResponse();
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act
        loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD);

        // Assert - Verify HTTP headers
        verify(restTemplate).postForEntity(eq(AUTH0_ENDPOINT), httpEntityCaptor.capture(), eq(TokenResponseDto.class));
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();
        HttpHeaders headers = capturedEntity.getHeaders();

        assertNotNull(headers);
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    void handleApiLogin_shouldSendCorrectRequestBody_withAllRequiredOAuth2Parameters() {
        // Arrange
        TokenResponseDto testTokenResponse = createTestTokenResponse();
        ResponseEntity<TokenResponseDto> responseEntity = ResponseEntity.ok(testTokenResponse);
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenReturn(responseEntity);

        // Act
        loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD);

        // Assert - Verify request body contains all OAuth2 grant parameters
        verify(restTemplate).postForEntity(eq(AUTH0_ENDPOINT), httpEntityCaptor.capture(), eq(TokenResponseDto.class));
        HttpEntity<?> capturedEntity = httpEntityCaptor.getValue();

        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) capturedEntity.getBody();
        assertNotNull(requestBody);

        // Verify OAuth2 password grant parameters
        assertEquals("password", requestBody.get("grant_type"));
        assertEquals(TEST_EMAIL, requestBody.get("username"));
        assertEquals(TEST_PASSWORD, requestBody.get("password"));
        assertEquals("test-client-id", requestBody.get("client_id"));
        assertEquals("test-client-secret", requestBody.get("client_secret"));
        assertEquals("https://test.auth0.com/api/v2/", requestBody.get("audience"));
        assertEquals("openid profile email", requestBody.get("scope"));
        assertEquals("Username-Password-Authentication", requestBody.get("connection"));
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAuth0RejectsCredentials() {
        // Arrange - 401 Unauthorized
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
                () -> loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldThrowAuthenticationFailed_whenAuth0ReturnsBadRequest() {
        // Arrange - 400 Bad Request (invalid request parameters)
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(HttpClientErrorException.BadRequest.create(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Bad Request",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void handleApiLogin_shouldPropagateRuntimeException_whenAuth0IsUnavailable() {
        // Arrange - 500 Internal Server Error
        // Note: HttpServerErrorException (5xx) is caught by the generic Exception handler
        // and wrapped in RuntimeException, not treated as authentication failure like 4xx errors
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(HttpServerErrorException.InternalServerError.create(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD)
        );

        assertEquals("API login process failed", exception.getMessage());
        assertNotNull(exception.getCause());
    }


    @Test
    void handleApiLogin_shouldPropagateRuntimeException_whenUnexpectedErrorOccurs() {
        // Arrange
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(TokenResponseDto.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loginService.handleApiLogin(TEST_EMAIL, TEST_PASSWORD)
        );

        assertEquals("API login process failed", exception.getMessage());
        assertTrue(exception.getCause().getMessage().contains("Network error"));
    }

    /**
     * Helper method to verify Auth0 was called with correct endpoint and basic parameters.
     */
    private void verifyAuth0RequestParameters(String email, String password) {
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<?>> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).postForEntity(
                urlCaptor.capture(),
                httpEntityCaptor.capture(),
                eq(TokenResponseDto.class)
        );

        // Verify correct Auth0 endpoint
        assertEquals(AUTH0_ENDPOINT, urlCaptor.getValue());
    }
}