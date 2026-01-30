package org.localhost.wmsemployee.service.employee;

import auth.dto.role.Auth0RoleDto;
import auth.error.Auth0ErrorCode;
import auth.exceptions.Auth0ConnectionException;
import auth.exceptions.Auth0PermissionException;
import auth.exceptions.Auth0ServiceException;
import auth.service.Auth0ManagementTokenService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Auth0 role metadata.
 * Fetches and caches Auth0 role IDs to support role assignment operations.
 * <p>
 * On application startup, this service queries the Auth0 Management API to retrieve
 * all configured roles and stores them in an in-memory cache (roleName → roleId).
 * This eliminates the need for hardcoded role IDs in the application.
 */
@Slf4j
public class Auth0RoleService {

    private final RestClient restClient;
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final Map<String, String> roleCache = new ConcurrentHashMap<>();

    @Value("${auth0.api.roles-endpoint}")
    private String auth0RolesEndpoint;

    public Auth0RoleService(RestClient restClient, Auth0ManagementTokenService auth0ManagementTokenService) {
        this.restClient = restClient;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
    }

    /**
     * Initializes the role cache by fetching all roles from Auth0 on application startup.
     * This method is called automatically after dependency injection.
     * <p>
     * Requires Auth0 Management API token with 'read:roles' scope.
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Fetching Auth0 roles from: {}", auth0RolesEndpoint);
            fetchAndCacheRoles();
            log.info("Successfully cached {} Auth0 roles: {}", roleCache.size(), roleCache.keySet());
        } catch (Exception e) {
            log.error("Failed to initialize Auth0 role cache on startup. Role assignment will fail until roles are loaded.", e);
            // Don't throw exception - allow application to start, but role operations will fail gracefully
        }
    }

    /**
     * Fetches all roles from Auth0 Management API and populates the role cache.
     *
     * @throws Auth0PermissionException if management token lacks required permissions
     * @throws Auth0ConnectionException if unable to connect to Auth0 service
     * @throws Auth0ServiceException    for other Auth0 API errors
     */
    private void fetchAndCacheRoles() {
        String managementToken = auth0ManagementTokenService.getAccessToken();

        try {
            List<Auth0RoleDto> roles = restClient.get()
                    .uri(auth0RolesEndpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + managementToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (roles == null || roles.isEmpty()) {
                log.warn("No roles returned from Auth0. Ensure roles are configured in Auth0 dashboard.");
                return;
            }

            roleCache.clear();
            for (Auth0RoleDto role : roles) {
                roleCache.put(role.name(), role.id());
                log.debug("Cached Auth0 role: {} -> {}", role.name(), role.id());
            }

        } catch (HttpClientErrorException e) {
            HttpStatus statusCode = (HttpStatus) e.getStatusCode();

            if (statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN) {
                log.error("Auth0 permission denied when fetching roles ({}) - Ensure M2M token has 'read:roles' scope. Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0PermissionException(
                        Auth0ErrorCode.PERMISSION_DENIED.getMessage(), e);

            } else {
                log.error("Unexpected Auth0 client error when fetching roles ({}) - Response: {}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                throw new Auth0ServiceException(
                        Auth0ErrorCode.UNEXPECTED_ERROR.formatMessage(e.getMessage()), e);
            }

        } catch (HttpServerErrorException e) {
            log.error("Auth0 service error when fetching roles ({}) - Response: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.SERVICE_UNAVAILABLE.getMessage(), e);

        } catch (ResourceAccessException e) {
            log.error("Failed to connect to Auth0 when fetching roles: {}", e.getMessage());
            throw new Auth0ConnectionException(
                    Auth0ErrorCode.NETWORK_ERROR.getMessage(), e);
        }
    }

    /**
     * Retrieves the Auth0 role ID for a given role name.
     *
     * @param roleName The Auth0 role name (e.g., "user", "supervisor", "admin")
     * @return The Auth0 role ID
     * @throws IllegalArgumentException if the role name is not found in the cache
     */
    public String getRoleIdByName(String roleName) {
        String roleId = roleCache.get(roleName);

        if (roleId == null) {
            log.error("Role '{}' not found in Auth0 role cache. Available roles: {}", roleName, roleCache.keySet());
            throw new IllegalArgumentException(
                    String.format("Auth0 role '%s' not found. Ensure the role exists in Auth0 dashboard and the application has restarted.", roleName)
            );
        }

        return roleId;
    }

    /**
     * Refreshes the role cache by fetching the latest roles from Auth0.
     * Useful if roles are added/modified in Auth0 after application startup.
     */
    public void refreshRoleCache() {
        log.info("Manually refreshing Auth0 role cache...");
        fetchAndCacheRoles();
    }
}
