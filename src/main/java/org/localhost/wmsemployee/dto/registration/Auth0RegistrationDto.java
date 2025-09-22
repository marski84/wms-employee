package org.localhost.wmsemployee.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO representing the response from Auth0 after successful user registration.
 * This class maps the fields returned by Auth0's Management API when creating a new user.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Auth0RegistrationDto {

    @JsonProperty("user_id")
    private String userId;

    private String email;

    private String name;

    private String nickname;

    private String username;

    @JsonProperty("created_at")
    private String createdAt;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class UserMetadataDto {

        private String phoneNumber;

        private String address;

        private String city;

        private String postalCode;

        private String country;

        private String roleId;

        private String roleName;

        private String familyName;
    }
}
