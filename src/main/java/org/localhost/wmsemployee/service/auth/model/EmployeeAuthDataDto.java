package org.localhost.wmsemployee.service.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmployeeAuthDataDto {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("connection")
    private String connection;

    @JsonProperty("email_verified")
    private boolean emailVerified;

    @JsonProperty("family_name")
    private String familyName;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("username")
    private String username;

    @JsonProperty("user_metadata")
    private UserMetadataDto userMetadata;

    public static EmployeeAuthDataDto fromEmployee(EmployeeRegistrationDto registrationDto) {
        if (registrationDto == null) {
            return null;
        }

        String derivedNickname = registrationDto.getName();
        String derivedUsername = (registrationDto.getEmail() != null) ?
                registrationDto.getEmail().split("@")[0] :
                (registrationDto.getName() + registrationDto.getSurname()).toLowerCase().replaceAll("\\s+", "");

        UserMetadataDto metadata = null;
        if (registrationDto.getPhoneNumber() != null) {
            metadata = UserMetadataDto.builder()
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
                .familyName(registrationDto.getSurname())
                .name(registrationDto.getName() + " " + registrationDto.getSurname())
                .nickname(derivedNickname)
                .username(derivedUsername)
                .userMetadata(metadata)
                .build();
    }
}