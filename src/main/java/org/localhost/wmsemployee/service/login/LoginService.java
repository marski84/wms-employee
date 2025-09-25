package org.localhost.wmsemployee.service.login;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.TokenResponseDto;
import org.localhost.wmsemployee.exceptions.AuthenticationFailedException;
import org.localhost.wmsemployee.exceptions.UserNotFoundException;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.localhost.wmsemployee.service.employee.EmployeeDataService;
import org.localhost.wmsemployee.service.employee.EmployeeQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

    private final EmployeeDataService employeeDataService;
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final EmployeeQueryService employeeQueryService;
    private final RestTemplate restTemplate;

    @Value("${auth0.m2m.audience}")
    private String audience;

    @Value("${auth0.m2m.domain}")
    private String domain;

    @Value("${auth0.m2m.clientId}")
    private String clientId;

    @Value("${auth0.m2m.clientSecret}")
    private String clientSecret;

    /**
     * Constructs a new LoginService with required dependencies.
     *
     * @param employeeDataService         Service for accessing employee data
     * @param auth0ManagementTokenService Service for obtaining Auth0 management tokens
     * @param restTemplate                RestTemplate for making HTTP requests to Auth0
     */
    public LoginService(EmployeeDataService employeeDataService,
                        Auth0ManagementTokenService auth0ManagementTokenService, EmployeeQueryService employeeQueryService,
                        RestTemplate restTemplate) {
        this.employeeDataService = employeeDataService;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
        this.employeeQueryService = employeeQueryService;
        this.restTemplate = restTemplate;
    }

    /**
     * Handles the login process for a user.
     *
     * @param email    The user's email address
     * @param password The user's password
     * @return Auth0UserDto containing the authenticated user's details
     * @throws AuthenticationFailedException if authentication fails
     * @throws IllegalArgumentException      if input parameters are invalid
     */
    public Auth0UserDto handleLogin(String email, String password) {
        log.debug("Attempting to authenticate user with email: {}", email);

        // Validate input parameters
        validateUserCredentials(email, password);

        String userId = findUserId(email);



        try {
            // 1. Authenticate user with Auth0 using their credentials
            String userAccessToken = authenticateWithAuth0(email, password);
            // 2. Get user details from Auth0 Management API only if authentication was successful
            return employeeQueryService.getEmployeeDetailsByUserId(userId, userAccessToken);
        } catch (HttpClientErrorException e) {
            log.error("Authentication failed for user {}: {}", email, e.getMessage());
            throw new AuthenticationFailedException("Invalid credentials");
        } catch (AuthenticationFailedException e) {
            log.error("Authentication failed for user {}: {}", email, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during login process for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Login process failed", e);
        }
    }

    private String findUserId(String email) {
        EmployeeData employeeData = employeeDataService.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User with email " + email + " not found")
        );
        return employeeData.getUserId();
    }

    /**
     * Validates the email address format.
     *
     * @param email The email address to validate
     * @throws IllegalArgumentException if the email is null, empty, or not in a valid format
     * @throws UserNotFoundException    if user is not found
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
     * Authenticates a user with Auth0 using their email and password.
     *
     * @param email    The user's email address
     * @param password The user's password
     * @return The access token if authentication is successful
     * @throws AuthenticationFailedException if authentication fails
     * @throws IllegalArgumentException      if input parameters are invalid
     */
    private String authenticateWithAuth0(String email, String password) {
        // Input validation is already done in handleLogin method

        String clearDomain = domain.trim().replaceAll("\\s+", "");

        String authUrl = "https://" + clearDomain + "/oauth/token";
        log.debug("Attempting to authenticate user with email in url: {}", authUrl);

        HttpEntity<Map<String, Object>> request = createAuthenticationRequest(email, password);

        try {
            ResponseEntity<TokenResponseDto> response = restTemplate.postForEntity(
                    authUrl, request, TokenResponseDto.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody().getAccess_token();
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
        requestBody.put("connection", "Username-Password-Authentication");

        return new HttpEntity<>(requestBody, headers);
    }
}
