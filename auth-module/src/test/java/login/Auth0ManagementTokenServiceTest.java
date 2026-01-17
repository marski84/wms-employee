package login;

import auth.dto.login.ManagementTokenResponse;
import auth.exceptions.InvalidAuth0TokenResponseException;
import auth.service.Auth0ManagementTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Auth0ManagementTokenServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;


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

    /**
     * Sets up RestClient mock chain to return a successful response.
     *
     * @param tokenResponse The token response to return
     */
    private void mockSuccessfulTokenResponse(ManagementTokenResponse tokenResponse) {
        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(ManagementTokenResponse.class))
                .thenReturn(responseEntity);
    }

    /**
     * Sets up RestClient mock chain to throw an exception.
     *
     * @param exception The exception to throw
     */
    private void mockRestClientException(Exception exception) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(ManagementTokenResponse.class))
                .thenThrow(exception);
    }

    /**
     * Sets up RestClient mock chain to return a response with null body.
     */
    private void mockRestClientNullResponse() {
        ResponseEntity<ManagementTokenResponse> responseEntity = ResponseEntity.ok(null);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(ManagementTokenResponse.class))
                .thenReturn(responseEntity);
    }

    /**
     * Sets up RestClient mock chain to return different responses on successive calls.
     * Use this when testing scenarios that require multiple API calls with different results.
     *
     * @param first  The response to return on the first call
     * @param second The response to return on the second call
     */
    private void mockSequentialTokenResponses(ManagementTokenResponse first, ManagementTokenResponse second) {
        ResponseEntity<ManagementTokenResponse> firstResponse = ResponseEntity.ok(first);
        ResponseEntity<ManagementTokenResponse> secondResponse = ResponseEntity.ok(second);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(ManagementTokenResponse.class))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);
    }



    // ========== getAccessToken() Tests ==========
    @Test
    void getAccessToken_shouldReturnNewToken_whenNoTokenCached() {
        // Given
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);
        mockSuccessfulTokenResponse(tokenResponse);
        // When
        String result = tokenService.getAccessToken();

        // Then
        assertNotNull(result);
        assertEquals(testAccessToken, result);
        verify(restClient, times(1)).post();
    }

    @Test
    void getAccessToken_shouldReturnCachedToken_whenTokenIsValid() {
        // Given - First call to cache the token
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                testAccessToken, "openid profile", "Bearer", expiresIn);

        mockSuccessfulTokenResponse(tokenResponse);

        // When - First call caches the token
        String firstToken = tokenService.getAccessToken();

        // Second call should return cached token
        String secondToken = tokenService.getAccessToken();

        // Then
        assertEquals(testAccessToken, firstToken);
        assertEquals(testAccessToken, secondToken);
        // Verify API was called only once (second call used cache)
        verify(restClient, times(1)).post();
    }

    @Test
    void getAccessToken_shouldRefreshToken_whenTokenIsExpired() {
        // Given - Set up expired token
        ManagementTokenResponse expiredTokenResponse = new ManagementTokenResponse(
                "expired-token", "openid profile", "Bearer", 1); // Very short expiry

        ManagementTokenResponse newTokenResponse = new ManagementTokenResponse(
                "new-token", "openid profile", "Bearer", expiresIn);

        mockSequentialTokenResponses(expiredTokenResponse, newTokenResponse);

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
        verify(restClient, times(2)).post();
    }

    @Test
    void getAccessToken_shouldThrowException_whenAccessTokenIsNull() {
        // Given - Response with null access_token
        ManagementTokenResponse tokenResponse = new ManagementTokenResponse(
                null, "openid profile", "Bearer", expiresIn);

        mockRestClientNullResponse();

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
        mockRestClientException(HttpClientErrorException.Unauthorized.create(
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
        mockRestClientException(new RuntimeException("Network error")
        );

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
        mockRestClientNullResponse();

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

        mockSuccessfulTokenResponse(tokenResponse);
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

        mockSuccessfulTokenResponse(tokenResponse);


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

        mockSuccessfulTokenResponse(tokenResponse);

        // When
        tokenService.getAccessToken();

        // Then - Verify request was made (implicitly tests createTokenRequest)
        verify(restClient).post();
    }

}