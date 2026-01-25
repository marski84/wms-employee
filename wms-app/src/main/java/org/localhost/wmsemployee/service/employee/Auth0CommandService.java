package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import auth.dto.registration.Auth0UserCreationRequest;
import auth.dto.role.AssignRoleRequestDto;
import auth.dto.role.Auth0RoleDto;
import auth.error.Auth0ErrorCode;
import auth.exceptions.*;
import auth.service.Auth0ManagementTokenService;
import employee.dto.CreateUserDto;
import employee.model.enumeration.EmployeeRole;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
public class Auth0CommandService {
    private final RestClient restClient;
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final Auth0RoleService auth0RoleService;

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.api.users-endpoint}")
    private String auth0UsersEndpoint;

    @Value("${auth0.connection:Username-Password-Authentication}")
    private String auth0Connection;

    public Auth0CommandService(RestClient restClient,
                               Auth0ManagementTokenService auth0ManagementTokenService,
                               Auth0RoleService auth0RoleService) {
        this.restClient = restClient;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
        this.auth0RoleService = auth0RoleService;
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
     * @throws Auth0UserAlreadyExistsException if user with email already exists in Auth0
     * @throws Auth0ValidationException if request payload is invalid or password too weak
     * @throws Auth0PermissionException if management token lacks required permissions
     * @throws Auth0ConnectionException if unable to connect to Auth0 service
     * @throws Auth0ServiceException for other Auth0 API errors
     */
    private Auth0RegistrationDto createAuth0User(CreateUserDto createUserDto) {
        String managementToken = auth0ManagementTokenService.getAccessToken();

        // Generate username from email (extract part before @) with defensive check
        String username = createUserDto.email().contains("@")
                ? createUserDto.email().split("@")[0]
                : createUserDto.email();

        // Generate nickname from name and surname
        String nickname = createUserDto.name() + " " + createUserDto.surname();

        // Build user creation request using type-safe DTO
        Auth0UserCreationRequest request = Auth0UserCreationRequest.builder()
                .email(createUserDto.email())
                .password(createUserDto.password())
                .connection(auth0Connection)
                .emailVerified(false)
                .username(username)
                .nickname(nickname)
                .userMetadata(Auth0UserCreationRequest.UserMetadata.builder()
                        .name(createUserDto.name())
                        .surname(createUserDto.surname())
                        .phoneNumber(createUserDto.phoneNumber())
                        .jobTitle(createUserDto.jobTitle())
                        .role(createUserDto.role() != null ? createUserDto.role().name() : null)
                        .status(createUserDto.status() != null ? createUserDto.status().name() : null)
                        .build())
                .build();

        log.info("Creating user in Auth0 using Management API: {} with connection: {}",
                auth0UsersEndpoint, auth0Connection);

        try {
            return restClient.post()
                    .uri(auth0UsersEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                    .body(request)
                    .retrieve()
                    .body(Auth0RegistrationDto.class);

        } catch (HttpClientErrorException e) {
            // Handle specific HTTP status codes
            HttpStatus statusCode = (HttpStatus) e.getStatusCode();

            if (statusCode == HttpStatus.CONFLICT) {
                log.error("User already exists in Auth0: {} - Response: {}",
                        createUserDto.email(), e.getResponseBodyAsString());
                throw new Auth0UserAlreadyExistsException(
                        Auth0ErrorCode.USER_ALREADY_EXISTS.formatMessage(createUserDto.email()), e);

            } else if (statusCode == HttpStatus.BAD_REQUEST) {
                log.error("Invalid user data for Auth0: {} - Response: {}",
                        createUserDto.email(), e.getResponseBodyAsString());
                throw new Auth0ValidationException(
                        Auth0ErrorCode.VALIDATION_ERROR.getMessage(), e);

            } else if (statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN) {
                log.error("Auth0 permission denied ({}) - Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0PermissionException(
                        Auth0ErrorCode.PERMISSION_DENIED.getMessage(), e);

            } else {
                log.error("Unexpected Auth0 client error ({}) - Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0ServiceException(
                        Auth0ErrorCode.UNEXPECTED_ERROR.formatMessage(e.getMessage()), e);
            }

        } catch (HttpServerErrorException e) {
            log.error("Auth0 service error ({}) - Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.SERVICE_UNAVAILABLE.getMessage(), e);

        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Auth0: {}", e.getMessage());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.NETWORK_ERROR.getMessage(), e);
        }
    }

    /**
     * Assigns a role to an Auth0 user.
     *
     * @param employeeRole The employee role to assign
     * @param auth0UserId  The Auth0 user ID
     * @throws Auth0PermissionException if management token lacks required permissions
     * @throws Auth0ConnectionException if unable to connect to Auth0 service
     * @throws Auth0ServiceException    for other Auth0 API errors
     * @throws IllegalArgumentException if the role is not mapped to Auth0
     */
    private void assignRoleToUser(EmployeeRole employeeRole, String auth0UserId) {
        String managementToken = auth0ManagementTokenService.getAccessToken();
        String roleEndpoint = auth0UsersEndpoint + "/" + auth0UserId + "/roles";

        // Convert EmployeeRole to Auth0 role name and get the role ID
        String auth0RoleName = RoleMapper.toAuth0RoleName(employeeRole);
        String roleId = auth0RoleService.getRoleIdByName(auth0RoleName);

        // Create request with role ID
        AssignRoleRequestDto requestBody = AssignRoleRequestDto.of(roleId);

        log.info("Assigning role '{}' (ID: {}) to Auth0 user: {}", auth0RoleName, roleId, auth0UserId);

        try {
            restClient.post()
                    .uri(roleEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully assigned role '{}' to user {}", auth0RoleName, auth0UserId);

        } catch (HttpClientErrorException e) {
            HttpStatus statusCode = (HttpStatus) e.getStatusCode();

            if (statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN) {
                log.error("Auth0 permission denied when assigning role ({}) - Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0PermissionException(
                        Auth0ErrorCode.PERMISSION_DENIED.getMessage(), e);

            } else if (statusCode == HttpStatus.NOT_FOUND) {
                log.error("User or role not found in Auth0 - Response: {}",
                        e.getResponseBodyAsString());
                throw new Auth0ValidationException(
                        Auth0ErrorCode.VALIDATION_ERROR.getMessage(), e);

            } else {
                log.error("Unexpected Auth0 client error when assigning role ({}) - Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0ServiceException(
                        Auth0ErrorCode.UNEXPECTED_ERROR.formatMessage(e.getMessage()), e);
            }

        } catch (HttpServerErrorException e) {
            log.error("Auth0 service error when assigning role ({}) - Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.SERVICE_UNAVAILABLE.getMessage(), e);

        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Auth0 when assigning role: {}", e.getMessage());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.NETWORK_ERROR.getMessage(), e);
        }
    }

    /**
     * Updates a user's role in Auth0 by removing all existing roles and assigning a new one.
     * This operation is atomic from Auth0's perspective - if assignment fails, the user
     * will have no roles until fixed.
     *
     * @param auth0UserId The Auth0 user ID
     * @param newRole     The new employee role to assign
     * @throws Auth0PermissionException if management token lacks required permissions
     * @throws Auth0ConnectionException if unable to connect to Auth0 service
     * @throws Auth0ServiceException    for other Auth0 API errors
     * @throws IllegalArgumentException if the role is not mapped to Auth0
     */
    public void updateUserRole(String auth0UserId, EmployeeRole newRole) {
        String managementToken = auth0ManagementTokenService.getAccessToken();
        String roleEndpoint = auth0UsersEndpoint + "/" + auth0UserId + "/roles";

        log.info("Updating role for Auth0 user {} to {}", auth0UserId, newRole);

        try {
            // Step 1: Get current roles
            List<Auth0RoleDto> currentRoles = getCurrentUserRoles(auth0UserId, managementToken, roleEndpoint);

            // Step 2: Delete all existing roles
            if (!currentRoles.isEmpty()) {
                deleteUserRoles(auth0UserId, managementToken, roleEndpoint, currentRoles);
            }

            // Step 3: Assign new role
            assignRoleToUser(newRole, auth0UserId);

        } catch (Exception e) {
            log.error("Failed to update role for user {}. User may have inconsistent role state in Auth0.", auth0UserId, e);
            throw e;
        }
    }

    /**
     * Retrieves the current roles assigned to an Auth0 user.
     */
    private List<Auth0RoleDto> getCurrentUserRoles(String auth0UserId, String managementToken, String roleEndpoint) {
        try {
            List<Auth0RoleDto> roles = restClient.get()
                    .uri(roleEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Auth0RoleDto>>() {
                    });

            log.debug("User {} currently has {} role(s)", auth0UserId, roles != null ? roles.size() : 0);
            return roles != null ? roles : List.of();

        } catch (HttpClientErrorException e) {
            log.error("Failed to get current roles for user {}: {}", auth0UserId, e.getResponseBodyAsString());
            throw new Auth0ServiceException(
                    Auth0ErrorCode.UNEXPECTED_ERROR.formatMessage("Failed to retrieve current roles"), e);
        }
    }

    /**
     * Deletes all roles from an Auth0 user.
     */
    private void deleteUserRoles(String auth0UserId, String managementToken, String roleEndpoint, List<Auth0RoleDto> rolesToDelete) {
        AssignRoleRequestDto deleteRequest = new AssignRoleRequestDto(
                rolesToDelete.stream().map(Auth0RoleDto::id).toList()
        );

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(roleEndpoint)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                    .body(deleteRequest)
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Deleted {} existing role(s) from user {}", rolesToDelete.size(), auth0UserId);

        } catch (HttpClientErrorException e) {
            log.error("Failed to delete roles from user {}: {}", auth0UserId, e.getResponseBodyAsString());
            throw new Auth0ServiceException(
                    Auth0ErrorCode.UNEXPECTED_ERROR.formatMessage("Failed to delete existing roles"), e);
        }
    }
}
