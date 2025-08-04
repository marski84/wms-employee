package org.localhost.wmsemployee.service.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.exceptions.InvalidAuth0TokenResponseException;
import org.localhost.wmsemployee.service.auth.model.ManagementTokenResponse;
import org.localhost.wmsemployee.service.auth.model.ManagmentTokenRequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


@Service
@Slf4j
public class Auth0ManagementTokenService {

    private static final long EXPIRY_BUFFER_SECONDS = 5 * 600;
    private final String CLIENT_CREDENTIALS = "client_credentials";
    private final RestTemplate restTemplate;
    private final ReentrantLock refreshLock = new ReentrantLock();


    @Value("${auth0.m2m.clientId}")
    private String clientId;
    @Value("${auth0.m2m.clientSecret}")
    private String clientSecret;
    @Value("${auth0.m2m.audience}")
    private String audience;
    @Value("${auth0.management.token-url}")
    private String tokenUrl;
    private volatile String cachedAccessToken;
    private volatile ZonedDateTime tokenExpiryTime;

    public Auth0ManagementTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            log.error("Initializing Auth0ManagementTokenService ... {}", clientId);
            getAccessToken();
        } catch (Exception e) {
            log.error("Failed to initialize Auth0 Management API token on startup", e);
        }
    }


    /**
     * Checks if the current token is valid and not expired.
     *
     * @param currentTime The current time to check against
     * @return true if the token is valid and not expired, false otherwise
     */
    protected boolean isTokenValid(ZonedDateTime currentTime) {
        return cachedAccessToken != null && tokenExpiryTime != null && currentTime.isBefore(tokenExpiryTime);
    }

    /**
     * Creates the HTTP request entity for the Auth0 token request.
     *
     * @return HttpEntity containing the request headers and body
     */
    protected HttpEntity<ManagmentTokenRequestBody> createTokenRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ManagmentTokenRequestBody requestBody = ManagmentTokenRequestBody.builder()
                .audience(audience)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(CLIENT_CREDENTIALS)
                .build();

        return new HttpEntity<>(requestBody, headers);
    }

    /**
     * Processes the token response from Auth0 and updates the cached token.
     *
     * @param responseBody The response body from Auth0
     * @return The new access token
     * @throws InvalidAuth0TokenResponseException if the response is invalid
     */
    protected String processTokenResponse(ManagementTokenResponse responseBody) {
        String newAccessToken = responseBody.getAccess_token();
        Number expiresIn = responseBody.getExpires_in();

        if (newAccessToken == null || expiresIn == null) {
            log.error("Failed to refresh Auth0 token: access_token or expires_in missing in response. Body: {}", responseBody);
            throw new InvalidAuth0TokenResponseException("Invalid token response from Auth0");
        }

        this.cachedAccessToken = newAccessToken;
        this.tokenExpiryTime = ZonedDateTime.now().plusSeconds(expiresIn.longValue() - EXPIRY_BUFFER_SECONDS);
        log.info("Successfully refreshed Auth0 Management API token. New expiry (with buffer): {}", this.tokenExpiryTime);

        return this.cachedAccessToken;
    }

    /**
     * Handles exceptions that occur during token refresh.
     *
     * @param e The exception that occurred
     * @throws InvalidAuth0TokenResponseException with appropriate message
     */
    protected void handleTokenRefreshException(Exception e) {
        if (e instanceof HttpClientErrorException httpError) {
            log.error("HttpClientErrorException while refreshing Auth0 token. Status: {}, Body: {}",
                    httpError.getStatusCode(), httpError.getResponseBodyAsString(), httpError);
            throw new InvalidAuth0TokenResponseException("Failed to refresh Auth0 token due to client error", httpError);
        } else {
            log.error("Unexpected exception while refreshing Auth0 token", e);
            throw new InvalidAuth0TokenResponseException("Failed to refresh Auth0 token", e);
        }
    }

    /**
     * Gets a valid access token, either from cache or by refreshing from Auth0.
     *
     * @return A valid access token
     */
    public String getAccessToken() {
        ZonedDateTime now = ZonedDateTime.now();
        if (isTokenValid(now)) {
            log.debug("Returning cached Auth0 Management API token.");
            return cachedAccessToken;
        }

        refreshLock.lock();
        try {
            now = ZonedDateTime.now();
            if (isTokenValid(now)) {
                log.debug("Token refreshed by another thread, returning new cached token.");
                return cachedAccessToken;
            }

            log.info("Auth0 Management API token is expired or missing. Refreshing...");
            HttpEntity<ManagmentTokenRequestBody> request = createTokenRequest();

            try {
                ResponseEntity<ManagementTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, ManagementTokenResponse.class);
                ManagementTokenResponse responseBody = Objects.requireNonNull(response.getBody());
                return processTokenResponse(responseBody);
            } catch (Exception e) {
                handleTokenRefreshException(e);
                // This line will never be reached as handleTokenRefreshException always throws an exception
                return null;
            }
        } finally {
            refreshLock.unlock();
        }
    }


}
