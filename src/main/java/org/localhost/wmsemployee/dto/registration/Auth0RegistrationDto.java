package org.localhost.wmsemployee.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

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

        private String familyName;

        private String city;

        private String postalCode;

        private String country;

        private String roleId;

        private EmployeeRole roleName;

        private EmployeeStatus employeeStatus;

    }
}
