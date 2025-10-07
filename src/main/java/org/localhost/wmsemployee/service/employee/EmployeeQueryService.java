package org.localhost.wmsemployee.service.employee;

import jakarta.persistence.EntityNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmployeeQueryService {
    private static final Logger log = LogManager.getLogger(EmployeeQueryService.class);
    private final EmployeeDataService employeeDataService;
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final RestTemplate restTemplate;

    @Value("${auth0.api.users-endpoint}")
    private String usersEndpoint;


    public EmployeeQueryService(EmployeeDataService employeeDataService,
                                Auth0ManagementTokenService auth0ManagementTokenService,
                                RestTemplate restTemplate) {
        this.employeeDataService = employeeDataService;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
        this.restTemplate = restTemplate;
    }

    /**
     * Retrieves the details of an employee by their user ID.
     *
     * @param userId      The ID of the user.
     * @param accessToken The access token required to authenticate the request.
     * @return The details of the employee with the given user ID.
     */
    public Auth0UserDto getEmployeeDetailsByUserId(String userId, String accessToken) {
        String userDetailsUrl = usersEndpoint + "/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Auth0UserDto> userResponse = restTemplate.getForEntity(userDetailsUrl, Auth0UserDto.class, headers);
        return userResponse.getBody();
    }

    /**
     * Retrieves the details of an employee by their user ID.
     * This method does not require an access token and uses the management token service to authenticate.
     *
     * @param userId The ID of the user.
     * @return The details of the employee with the given user ID.
     */
    public Auth0UserDto getEmployeeDetailsByUserId(String userId) {
        try {
            String token = auth0ManagementTokenService.getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            String url = usersEndpoint + "/" + userId;
            log.debug("Making request to: {}", url);

            ResponseEntity<Auth0UserDto> userResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Auth0UserDto.class
            );
            log.info("Successfully retrieved employee details for user ID: {}", userId);
            return userResponse.getBody();
        } catch (Exception e) {
            log.error("Error getting employee details for user ID: " + userId, e);
            throw e;
        }
    }

    /**
     * Retrieves the details of an employee by their username.
     * First looks up the user in the local database to get their Auth0 user ID,
     * then fetches the full details from Auth0.
     *
     * @param username The username of the employee to look up
     * @return The details of the employee with the given username
     * @throws EntityNotFoundException if no employee is found with the given username
     */
    public Auth0UserDto getEmployeeDetailsByUsername(String username) {
        log.debug("Looking up employee with username: {}", username);

        EmployeeData employeeData = employeeDataService.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("No employee found with username: {}", username);
                    return new EmployeeNotFoundException();
                });

        log.debug("Found employee with ID: {}", employeeData.getUserId());

        return getEmployeeDetailsByUserId(employeeData.getUserId());
    }


}
