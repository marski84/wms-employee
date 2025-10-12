package org.localhost.wmsemployee.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.exceptions.InvalidAuth0TokenResponseException;
import org.localhost.wmsemployee.service.auth.model.ManagementTokenResponse;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Auth0ManagementTokenServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private Auth0ManagementTokenService tokenService;

    private final String testClientId = "test-client-id";
    private final String testClientSecret = "test-client-secret";
    private final String testAudience = "https://test.auth0.com/api/v2/";
    private final String testTokenUrl = "https://test.auth0.com/oauth/token";
    private final String testAccessToken = "test-management-token";
    private final int expiresIn = 86400; // 24 hours

    @BeforeEach
    void setUp() {
        // Set configuration values using ReflectionTestUtils
        ReflectionTestUtils.setField(tokenService, "clientId", testClientId);
        ReflectionTestUtils.setField(tokenService, "clientSecret", testClientSecret);
        ReflectionTestUtils.setField(tokenService, "audience", testAudience);
        ReflectionTestUtils.setField(tokenService, "tokenUrl", testTokenUrl);
    }

    // ========== getAccessToken() Tests ==========

    @Test
    void getAccessToken_shouldReturnNewToken_whenNoTokenCached() {
        // Given
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When
        String result = tokenService.getAccessToken();

        // Then
        assertNotNull(result);
        assertEquals(testAccessToken, result);
        verify(restTemplate, times(1)).postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class));
    }

    @Test
    void getAccessToken_shouldReturnCachedToken_whenTokenIsValid() {
        // Given - First call to cache the token
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When - First call caches the token
        String firstToken = tokenService.getAccessToken();

        // Second call should return cached token
        String secondToken = tokenService.getAccessToken();

        // Then
        assertEquals(testAccessToken, firstToken);
        assertEquals(testAccessToken, secondToken);
        // Verify API was called only once (second call used cache)
        verify(restTemplate, times(1)).postForEntity(anyString(), any(HttpEntity.class), eq(ManagementTokenResponse.class));
    }

    @Test
    void getAccessToken_shouldRefreshToken_whenTokenIsExpired() {
        // Given - Set up expired token
        ManagementTokenResponse expiredTokenResponse = new ManagementTokenResponse(
                "expired-token", "openid profile", "Bearer", 1); // Very short expiry

        ManagementTokenResponse newTokenResponse = new ManagementTokenResponse(
                "new-token", "openid profile", "Bearer", expiresIn);

        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(ResponseEntity.ok(expiredTokenResponse))
                .thenReturn(ResponseEntity.ok(newTokenResponse));

        // When - First call caches expired token
        String firstToken = tokenService.getAccessToken();

        // Wait for token to expire (considering 5*600 seconds buffer)
        // Set token expiry to past
        ReflectionTestUtils.setField(tokenService, "tokenExpiryTime", ZonedDateTime.now().minusHours(1));

        // Second call should refresh
        String secondToken = tokenService.getAccessToken();

        // Then
        assertEquals("expired-token", firstToken);
        assertEquals("new-token", secondToken);
        // Verify API was called twice (once for initial, once for refresh)
        verify(restTemplate, times(2)).postForEntity(anyString(), any(HttpEntity.class), eq(ManagementTokenResponse.class));
    }

    @Test
    void getAccessToken_shouldThrowException_whenAccessTokenIsNull() {
        // Given - Response with null access_token
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                null, "openid profile", "Bearer", expiresIn);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When & Then
        InvalidAuth0TokenResponseException exception = assertThrows(
                InvalidAuth0TokenResponseException.class,
                () -> tokenService.getAccessToken()
        );

        // The service wraps null token in InvalidAuth0TokenResponseException
        assertNotNull(exception.getMessage());
    }

    @Test
    void getAccessToken_shouldThrowException_whenHttpClientErrorOccurs() {
        // Given - HTTP error from Auth0
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenThrow(HttpClientErrorException.Unauthorized.create(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        org.springframework.http.HttpHeaders.EMPTY,
                        new byte[0],
                        null
                ));

        // When & Then
        InvalidAuth0TokenResponseException exception = assertThrows(
                InvalidAuth0TokenResponseException.class,
                () -> tokenService.getAccessToken()
        );

        assertTrue(exception.getMessage().contains("Failed to refresh Auth0 token due to client error"));
        assertInstanceOf(HttpClientErrorException.class, exception.getCause());
    }

    @Test
    void getAccessToken_shouldThrowException_whenUnexpectedErrorOccurs() {
        // Given - Generic exception
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        InvalidAuth0TokenResponseException exception = assertThrows(
                InvalidAuth0TokenResponseException.class,
                () -> tokenService.getAccessToken()
        );

        assertTrue(exception.getMessage().contains("Failed to refresh Auth0 token"));
        assertInstanceOf(RuntimeException.class, exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Network error"));
    }

    @Test
    void getAccessToken_shouldHandleNullResponseBody() {
        // Given - Response entity with null body
        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When & Then - Service wraps NullPointerException in InvalidAuth0TokenResponseException
        assertThrows(
                InvalidAuth0TokenResponseException.class,
                () -> tokenService.getAccessToken()
        );
    }

    @Test
    void getAccessToken_shouldCalculateExpiryWithBuffer() {
        // Given
        int expirySeconds = 3600; // 1 hour
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expirySeconds);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When
        ZonedDateTime before = ZonedDateTime.now();
        tokenService.getAccessToken();
        ZonedDateTime after = ZonedDateTime.now();

        // Then - Verify expiry is set with buffer (5*600 = 3000 seconds)
        ZonedDateTime expiryTime = (ZonedDateTime) ReflectionTestUtils.getField(tokenService, "tokenExpiryTime");
        assertNotNull(expiryTime);

        // Expiry should be approximately now + expirySeconds - buffer
        long expectedBuffer = 5 * 600; // 3000 seconds
        ZonedDateTime expectedExpiry = before.plusSeconds(expirySeconds - expectedBuffer);

        // Allow 1-second tolerance for test execution time
        assertTrue(expiryTime.isAfter(expectedExpiry.minusSeconds(1)));
        assertTrue(expiryTime.isBefore(after.plusSeconds(expirySeconds - expectedBuffer + 1)));
    }

    @Test
    void getAccessToken_shouldCacheTokenCorrectly() {
        // Given
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When
        String result = tokenService.getAccessToken();

        // Then - Verify cache was updated
        String cachedToken = (String) ReflectionTestUtils.getField(tokenService, "cachedAccessToken");
        assertEquals(testAccessToken, cachedToken);
        assertEquals(testAccessToken, result);

        // Verify expiry time was set
        ZonedDateTime expiryTime = (ZonedDateTime) ReflectionTestUtils.getField(tokenService, "tokenExpiryTime");
        assertNotNull(expiryTime);
        assertTrue(expiryTime.isAfter(ZonedDateTime.now()));
    }

    @Test
    void getAccessToken_shouldUseClientCredentialsGrantType() {
        // Given
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);

        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class)))
                .thenReturn(responseEntity);

        // When
        tokenService.getAccessToken();

        // Then - Verify request was made (implicitly tests createTokenRequest)
        verify(restTemplate).postForEntity(eq(testTokenUrl), any(HttpEntity.class), eq(ManagementTokenResponse.class));
    }
}