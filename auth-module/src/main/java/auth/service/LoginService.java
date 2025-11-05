package auth.service;

import auth.dto.login.TokenResponseDto;
import auth.exceptions.AuthenticationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for handling user authentication and login processes.
 * Implements stateless JWT authentication with Auth0 using the password grant flow.
 * <p>
 * This implementation follows SOLID principles:
 * - Single Responsibility: Handles only authentication logic
 * - Dependency Inversion: Implements IAuthenticationService interface
 * - Open/Closed: Can be extended or replaced without changing existing code
 */
@Service
@Slf4j
public class LoginService implements IAuthenticationService {
    // Constants for OAuth2 password grant flow
    private static final String GRANT_TYPE = "grant_type";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CLIENT_ID = "client_id";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String AUDIENCE = "audience";
    private static final String SCOPE = "scope";
    private static final String DEFAULT_SCOPE = "openid profile email";
    private static final String CONNECTION = "connection";
    private static final String AUTH0_TOKEN_ENDPOINT = "/oauth/token";
    private static final String HTTPS_PROTOCOL = "https://";

    private final RestTemplate restTemplate;

    @Value("${auth0.audience}")
    private String audience;

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.clientId}")
    private String clientId;

    @Value("${auth0.clientSecret}")
    private String clientSecret;

    @Value("${auth0.connection:Username-Password-Authentication}")
    private String auth0Connection;

    /**
     * Constructs a new LoginService with required dependencies.
     *
     * @param restTemplate RestTemplate for making HTTP requests to Auth0
     */
    public LoginService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Handles the API login process for a user and returns complete token information.
     *
     * @param email    The user's email address
     * @param password The user's password
     * @return TokenResponseDto containing JWT tokens
     * @throws AuthenticationFailedException if authentication fails
     * @throws IllegalArgumentException      if input parameters are invalid
     */
    public TokenResponseDto handleApiLogin(String email, String password) {
        log.debug("Attempting API authentication for user with email: {}", email);

//  TODO dodać roles do array permissions w auth0

        try {
            TokenResponseDto tokenResponse = authenticateAndGetTokens(email, password);
            log.info("API authentication successful for user: {}", email);
            return tokenResponse;
        } catch (HttpClientErrorException e) {
            log.error("API authentication failed for user {}: {}", email, e.getMessage());
            throw new AuthenticationFailedException("Invalid credentials");
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during API login process for user {}: {}", email, e.getMessage());
            throw new RuntimeException("API login process failed", e);
        }
    }

    /**
     * Authenticates with Auth0 and returns the complete token response.
     * This is the main authentication method that calls Auth0's OAuth2 token endpoint.
     *
     * @param email    The user's email address
     * @param password The user's password
     * @return TokenResponseDto containing access token, ID token, token type, and expiration
     * @throws AuthenticationFailedException if authentication fails
     */
    private TokenResponseDto authenticateAndGetTokens(String email, String password) {
        String authUrl = HTTPS_PROTOCOL + domain + AUTH0_TOKEN_ENDPOINT;
        log.debug("Attempting to authenticate user with Auth0: {}", authUrl);

        HttpEntity<Map<String, Object>> request = createAuthenticationRequest(email, password);

        try {
            ResponseEntity<TokenResponseDto> tokenResponse = restTemplate.postForEntity(
                    authUrl, request, TokenResponseDto.class);
            if (tokenResponse.getStatusCode().is2xxSuccessful()) {
                return tokenResponse.getBody();
            }

            throw new AuthenticationFailedException("Failed to authenticate with Auth0");
        } catch (HttpClientErrorException e) {
            log.error("Auth0 authentication failed: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private HttpEntity<Map<String, Object>> createAuthenticationRequest(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(GRANT_TYPE, GRANT_TYPE_PASSWORD);
        requestBody.put(USERNAME, email);
        requestBody.put(PASSWORD, password);
        requestBody.put(CLIENT_ID, clientId);
        requestBody.put(CLIENT_SECRET, clientSecret);
        requestBody.put(AUDIENCE, audience);
        requestBody.put(SCOPE, DEFAULT_SCOPE);
        requestBody.put(CONNECTION, auth0Connection);

        return new HttpEntity<>(requestBody, headers);
    }
}
