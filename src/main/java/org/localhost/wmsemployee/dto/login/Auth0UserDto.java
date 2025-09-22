package org.localhost.wmsemployee.dto.login;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;

import java.time.ZonedDateTime;

/**
 * Represents the response from Auth0 GET request.
 * Contains employee details including user metadata.
 */
@Getter
@Builder
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Auth0UserDto {

    @JsonProperty("created_at")
    @NotNull(message = "Created date cannot be null")
    private ZonedDateTime createdAt;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Nickname cannot be blank")
    private String nickname;

    @JsonProperty("updated_at")
    @NotNull(message = "Updated date cannot be null")
    private ZonedDateTime updatedAt;

    @JsonProperty("user_id")
    @NotBlank(message = "User ID cannot be blank")
    private String userId;

    @JsonProperty("user_metadata")
    @Valid
    @NotNull(message = "User metadata cannot be null")
    private UserMetadata userMetadata;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    /**
     * Represents user metadata in Auth0 response.
     */
    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    public static class UserMetadata {

        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = "^\\+[0-9]{1,3}[0-9]{4,14}$", message = "Phone number must be in international format (e.g., +48123456789)")
        private String phoneNumber;

        @NotBlank(message = "Address cannot be blank")
        private String address;

        @NotBlank(message = "City cannot be blank")
        private String city;

        @NotBlank(message = "Postal code cannot be blank")
        private String postalCode;

        @NotBlank(message = "Country cannot be blank")
        private String country;

        @NotBlank(message = "Role ID cannot be blank")
        private String roleId;

        @NotBlank(message = "Role name cannot be blank")
        @Enumerated(EnumType.STRING)
        private EmployeeRole roleName;

        @NotBlank(message = "Family name cannot be blank")
        private String familyName;
    }
}
