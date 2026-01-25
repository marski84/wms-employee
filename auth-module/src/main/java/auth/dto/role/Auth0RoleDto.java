package auth.dto.role;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing an Auth0 role response from the Management API.
 * Used when fetching roles from GET /api/v2/roles endpoint.
 */
public record Auth0RoleDto(
        @JsonProperty("id")
        String id,

        @JsonProperty("name")
        String name,

        @JsonProperty("description")
        String description
) {
}
