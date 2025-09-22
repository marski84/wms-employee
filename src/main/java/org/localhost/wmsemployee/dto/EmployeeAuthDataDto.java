package org.localhost.wmsemployee.dto;

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
        String derivedUsername = (registrationDto.getEmail() != null) ?
                registrationDto.getEmail().split("@")[0] :
                (registrationDto.getName() + registrationDto.getSurname()).toLowerCase().replaceAll("\\s+", "");

        Auth0RegistrationDto.UserMetadataDto metadata = null;
        if (registrationDto.getPhoneNumber() != null) {
            metadata = Auth0RegistrationDto.UserMetadataDto.builder()
                    .familyName(registrationDto.getSurname())
                    .phoneNumber(registrationDto.getPhoneNumber())
                    .roleId(String.valueOf(registrationDto.getEmployeeRole().getRoleId()))
                    .roleName(registrationDto.getEmployeeRole().getRoleName())
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