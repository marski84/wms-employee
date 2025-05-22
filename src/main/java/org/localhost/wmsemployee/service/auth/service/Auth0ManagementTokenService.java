package org.localhost.wmsemployee.service.auth.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

//    https://www.perplexity.ai/search/przygotuj-instrukcje-implement-WceALhycSXOSX8jQSHj6ig?0=d

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
            getAccessToken();
        } catch (Exception e) {
            log.error("Failed to initialize Auth0 Management API token on startup", e);
        }
    }


    public String getAccessToken() {
        ZonedDateTime now = ZonedDateTime.now();
        if (cachedAccessToken != null && tokenExpiryTime != null && now.isBefore(tokenExpiryTime)) {
            log.debug("Returning cached Auth0 Management API token.");
            return cachedAccessToken;
        }

        refreshLock.lock();
        try {
            now = ZonedDateTime.now();
            if (cachedAccessToken != null && tokenExpiryTime != null && now.isBefore(tokenExpiryTime)) {
                log.debug("Token refreshed by another thread, returning new cached token.");
                return cachedAccessToken;
            }

            log.info("Auth0 Management API token is expired or missing. Refreshing...");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ManagmentTokenRequestBody requestBody = ManagmentTokenRequestBody.builder()
                    .audience(audience).clientId(clientId).audience(audience).clientSecret(clientSecret)
                    .grantType(CLIENT_CREDENTIALS)
                    .build();
            HttpEntity<ManagmentTokenRequestBody> request = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<ManagementTokenResponse> response = restTemplate.postForEntity(tokenUrl, request, ManagementTokenResponse.class);

                ManagementTokenResponse responseBody = Objects.requireNonNull(response.getBody());
                String newAccessToken = responseBody.getAccess_token();
                Number expiresIn = responseBody.getExpires_in();

                if (newAccessToken == null || expiresIn == null) {
                    log.error("Failed to refresh Auth0 token: access_token or expires_in missing in response. Body: {}", responseBody);
                    throw new RuntimeException("Invalid token response from Auth0");
                }

                this.cachedAccessToken = newAccessToken;
                this.tokenExpiryTime = ZonedDateTime.now().plusSeconds(expiresIn.longValue() - EXPIRY_BUFFER_SECONDS);
                log.info("Successfully refreshed Auth0 Management API token. New expiry (with buffer): {}", this.tokenExpiryTime);
                return this.cachedAccessToken;

            } catch (HttpClientErrorException e) {
                log.error("HttpClientErrorException while refreshing Auth0 token. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new RuntimeException("Failed to refresh Auth0 token due to client error", e);
            } catch (Exception e) {
                log.error("Unexpected exception while refreshing Auth0 token", e);
                throw new RuntimeException("Failed to refresh Auth0 token", e);
            }
        } finally {
            refreshLock.unlock();
        }
    }


}
