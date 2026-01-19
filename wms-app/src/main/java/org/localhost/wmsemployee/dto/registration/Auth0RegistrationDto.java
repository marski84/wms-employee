package org.localhost.wmsemployee.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing the response from Auth0 after successful user registration.
 * Maps the essential fields returned by Auth0's Management API when creating a new user.
 *
 * The user_id field is critical - it's stored as auth0_user_id in the local User entity
 * to link the Auth0 identity with the local user record.
 */
public record Auth0RegistrationDto(
        @JsonProperty("user_id")
        String userId,

        String email,

        String nickname,

        String username,

        @JsonProperty("created_at")
        String createdAt
) {
}
