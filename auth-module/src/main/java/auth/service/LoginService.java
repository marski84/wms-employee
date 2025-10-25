package auth.service;

import auth.dto.login.TokenResponseDto;
import auth.exceptions.AuthenticationFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service responsible for handling user authentication and login processes.
 * This service interacts with Auth0 for user authentication and retrieves user details.
 */
@Service
@Slf4j
public class LoginService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_PASSWORD_LENGTH = 6;

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
     * @param restTemplate         RestTemplate for making HTTP requests to Auth0
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

        // Validate input parameters
        validateUserCredentials(email, password);

        try {
            // Authenticate with Auth0 FIRST to avoid user enumeration
            TokenResponseDto tokenResponse = authenticateAndGetTokens(email, password);

            // Check if user exists in our database (only after successful Auth0 authentication)
//            findUserIdOrThrowAuthException(email);

            log.info("API authentication successful for user: {}", email);
            logToken(tokenResponse);
            return tokenResponse;
        } catch (HttpClientErrorException e) {
            log.error("API authentication failed for user {}: {}", email, e.getMessage());
            throw new AuthenticationFailedException("Invalid credentials");
        } catch (AuthenticationFailedException e) {
            log.error("API authentication failed for user {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during API login process for user {}: {}", email, e.getMessage());
            throw new RuntimeException("API login process failed", e);
        }
    }

    void logToken(TokenResponseDto tokenResponseDto) throws JsonProcessingException {
        String token = tokenResponseDto.getAccess_token();
        String[] parts = token.split("\\.");

        if (parts.length == 3) {
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            log.info("Token Payload: {}", payload);

            // Or pretty print as JSON
            ObjectMapper mapper = new ObjectMapper();
            Object json = mapper.readValue(payload, Object.class);
            log.info("Token Payload (formatted): {}",
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        }

    }

//    /**
//     * Finds user ID by email, but throws AuthenticationFailedException instead of UserNotFoundException
//     * to prevent user enumeration attacks. This makes "user not found" indistinguishable from
//     * "invalid credentials" to potential attackers.
//     *
//     * @param email The user's email address
//     * @return The user's Auth0 user ID
//     * @throws AuthenticationFailedException if user is not found in database (appears as auth failure)
//     */
//    private String findUserIdOrThrowAuthException(String email) {
//        return authUserProvider.findUserByEmail(email)
//                .map(AuthUser::getUserId)
//                .orElseThrow(() -> {
//                    log.warn("User not found in database: {}", email);
//                    return new AuthenticationFailedException("Invalid credentials");
//                });
//    }

    /**
     * Validates user credentials (email and password format).
     *
     * @param email    The email address to validate
     * @param password The password to validate
     * @throws IllegalArgumentException if the email or password is invalid
     */
    private void validateUserCredentials(String email, String password) {
        if (!StringUtils.hasText(email)) {
            log.error("Email cannot be null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.error("Invalid email format: {}", email);
            throw new IllegalArgumentException("Invalid email format");
        }

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            log.error("Invalid password provided for user {}", email);
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
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
        String clearDomain = domain.trim().replaceAll("\\s+", "");
        String authUrl = "https://" + clearDomain + "/oauth/token";
        log.debug("Attempting to authenticate user with Auth0: {}", authUrl);

        HttpEntity<Map<String, Object>> request = createAuthenticationRequest(email, password);

        try {
            ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity(
                    authUrl, request, TokenResponseDto.class);

            TokenResponseDto tokenResponse = response.getBody();
            if (tokenResponse != null && tokenResponse.getAccess_token() != null) {
                return tokenResponse;
            } else {
                throw new AuthenticationFailedException("Failed to authenticate with Auth0");
            }
        } catch (HttpClientErrorException e) {
            log.error("Auth0 authentication failed: {}", e.getResponseBodyAsString());
            throw e;
        }
    }

    private HttpEntity<Map<String, Object>> createAuthenticationRequest(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Using standard password grant type for user authentication
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("grant_type", "password");
        requestBody.put("username", email);
        requestBody.put("password", password);
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("audience", audience);
        requestBody.put("scope", "openid profile email");
        requestBody.put("connection", auth0Connection);

        return new HttpEntity<>(requestBody, headers);
    }
}
