package auth.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * Request DTO for creating a new user in Auth0 via Management API.
 * <p>
 * This replaces the Map-based payload construction with a type-safe, self-documenting structure.
 * Jackson's ObjectMapper automatically converts this to JSON when sent via RestClient.
 * <p>
 * Uses @JsonProperty to map Java camelCase field names to Auth0's snake_case JSON format.
 */
@Builder
public record Auth0UserCreationRequest(
        String email,

        String password,

        String connection,

        @JsonProperty("email_verified")
        boolean emailVerified,

        String username,

        String nickname,

        @JsonProperty("user_metadata")
        UserMetadata userMetadata
) {

    /**
     * Nested record for user_metadata field in Auth0 user creation request.
     * Contains custom application-specific data stored in Auth0.
     */
    @Builder
    public record UserMetadata(
            String name,

            String surname,

            String phoneNumber,

            String jobTitle,

            String role,

            String status
    ) {
    }
}