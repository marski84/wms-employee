package org.localhost.wmsemployee.service.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.exceptions.NoValidDtoException;

import java.time.ZonedDateTime;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Slf4j
@Table(name = "employee")
public class EmployeeData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userId;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private ZonedDateTime createdAt;
    @Column(nullable = false)
    private ZonedDateTime updatedAt;

    /**
     * Creates an EmployeeData entity from Auth0RegistrationDto.
     *
     * @param auth0Dto The Auth0 registration DTO containing employee data
     * @return EmployeeData entity populated with data from the DTO
     * @throws NoValidDtoException if the provided DTO is null
     */
    public static EmployeeData fromAuth0Dto(Auth0RegistrationDto auth0Dto) {
        if (auth0Dto == null) {
            log.error("Auth0RegistrationDto is null");
            throw new NoValidDtoException();
        }

        return EmployeeData.builder()
                .userId(auth0Dto.getUserId())
                .name(auth0Dto.getName())
                .username(auth0Dto.getUsername())
                .nickname(auth0Dto.getNickname())
                .email(auth0Dto.getEmail())
                .createdAt(ZonedDateTime.parse(auth0Dto.getCreatedAt()))
                .updatedAt(ZonedDateTime.now())
                .build();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
