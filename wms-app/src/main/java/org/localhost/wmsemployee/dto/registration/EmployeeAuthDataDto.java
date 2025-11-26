package org.localhost.wmsemployee.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.localhost.wmsemployee.exceptions.NoValidDtoException;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmployeeAuthDataDto {

    private String email;

    private String password;

    private String connection;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    private String name;

    private String nickname;

    private String username;

    @JsonProperty("user_metadata")
    private Auth0RegistrationDto.UserMetadataDto userMetadata;

    public static EmployeeAuthDataDto fromEmployee(EmployeeRegistrationDto registrationDto) {
        if (registrationDto == null) {
            throw new NoValidDtoException();
        }

        String derivedNickname = registrationDto.getName();

        // Generate username from email or name, ensuring it's between 1-15 characters (Auth0 requirement)
        String derivedUsername;
        if (registrationDto.getEmail() != null) {
            String emailPrefix = registrationDto.getEmail().split("@")[0];
            derivedUsername = emailPrefix.length() > 15 ? emailPrefix.substring(0, 15) : emailPrefix;
        } else {
            String combined = (registrationDto.getName() + registrationDto.getSurname()).toLowerCase().replaceAll("\\s+", "");
            derivedUsername = combined.length() > 15 ? combined.substring(0, 15) : combined;
        }

        Auth0RegistrationDto.UserMetadataDto metadata = null;
        if (registrationDto.getPhoneNumber() != null) {
            metadata = Auth0RegistrationDto.UserMetadataDto.builder()
                    .familyName(registrationDto.getSurname())
                    .phoneNumber(registrationDto.getPhoneNumber())
                    .roleId(String.valueOf(registrationDto.getEmployeeRole().getRoleId()))
                    .roleName(registrationDto.getEmployeeRole())
                    .employeeStatus(registrationDto.getEmployeeStatus())
                    .address(registrationDto.getAddress())
                    .city(registrationDto.getCity())
                    .postalCode(registrationDto.getPostalCode())
                    .country(registrationDto.getCountry())
                    .build();
        }

        return EmployeeAuthDataDto.builder()
                .email(registrationDto.getEmail())
                .password(registrationDto.getPassword())
                .connection("Username-Password-Authentication")
                .emailVerified(true)
                .name(registrationDto.getName() + " " + registrationDto.getSurname())
                .nickname(derivedNickname)
                .username(derivedUsername)
                .userMetadata(metadata)
                .build();
    }
}