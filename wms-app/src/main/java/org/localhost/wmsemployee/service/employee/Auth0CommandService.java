package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import auth.service.Auth0ManagementTokenService;
import employee.dto.CreateUserDto;
import employee.model.enumeration.EmployeeRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class Auth0CommandService {
    private final RestClient restClient;
    private final Auth0ManagementTokenService auth0ManagementTokenService;

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.api.users-endpoint}")
    private String auth0UsersEndpoint;

    @Value("${auth0.connection:Username-Password-Authentication}")
    private String auth0Connection;

    public Auth0CommandService(RestClient restClient, Auth0ManagementTokenService auth0ManagementTokenService) {
        this.restClient = restClient;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
    }


    /**
     * Registers a new employee in Auth0 using the Management API.
     *
     * @param createUserDto The user creation data from employee-module
     * @return Auth0RegistrationDto containing the Auth0 response with user details
     */
    public Auth0RegistrationDto registerInAuth0(CreateUserDto createUserDto) {
        Auth0RegistrationDto auth0Response = createAuth0User(createUserDto);

        if (createUserDto.role() != null) {
            assignRoleToUser(createUserDto.role(), auth0Response.userId());
        }

        log.info("Employee successfully registered in Auth0. User ID: {}", auth0Response.userId());
        return auth0Response;
    }

    /**
     * Creates a new user in Auth0 using the Management API /api/v2/users endpoint.
     * Requires valid M2M authentication token with create:users scope.
     * Token is automatically managed and refreshed by Auth0ManagementTokenService.
     *
     * @param createUserDto The user creation data
     * @return Auth0RegistrationDto containing the created user details
     */
    private Auth0RegistrationDto createAuth0User(CreateUserDto createUserDto) {
        String managementToken = auth0ManagementTokenService.getAccessToken();

        // Build user creation payload for Management API
        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("email", createUserDto.email());
        userPayload.put("password", createUserDto.password());
        userPayload.put("connection", auth0Connection);
        userPayload.put("email_verified", false);

        // Generate username from email (extract part before @)
        String username = createUserDto.email().split("@")[0];
        userPayload.put("username", username);

        // Generate nickname from name and surname
        String nickname = createUserDto.name() + " " + createUserDto.surname();
        userPayload.put("nickname", nickname);

        // Store additional employee data in user_metadata
        Map<String, Object> userMetadata = new LinkedHashMap<>();
        userMetadata.put("name", createUserDto.name());
        userMetadata.put("surname", createUserDto.surname());
        userMetadata.put("phoneNumber", createUserDto.phoneNumber());
        userMetadata.put("jobTitle", createUserDto.jobTitle());
        userMetadata.put("role", createUserDto.role() != null ? createUserDto.role().name() : null);
        userMetadata.put("status", createUserDto.status() != null ? createUserDto.status().name() : null);
        userPayload.put("user_metadata", userMetadata);

        log.info("Creating user in Auth0 using Management API: {} with connection: {}",
                auth0UsersEndpoint, auth0Connection);

        return restClient.post()
                .uri(auth0UsersEndpoint)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                .body(userPayload)
                .retrieve()
                .body(Auth0RegistrationDto.class);
    }

    private void assignRoleToUser(EmployeeRole employeeRole, String auth0UserId) {
        // TODO: Implement role assignment using Auth0 Management API
        String roleEndpoint = auth0UsersEndpoint + "/" + auth0UserId + "/roles";
        log.debug("Role assignment endpoint: {}", roleEndpoint);
    }
}
