package org.localhost.wmsemployee.service.auth.model;

import org.junit.jupiter.api.Test;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.exceptions.NoValidDtoException;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeDataTest {

    @Test
    void fromAuth0Dto_shouldConvertValidDto() {
        // Arrange
        String userId = "auth0|123456";
        String email = "test@example.com";
        String name = "Test User";
        String nickname = "Test";
        String username = "testuser";
        String createdAt = ZonedDateTime.now().toString();

        Auth0RegistrationDto auth0Dto = Auth0RegistrationDto.builder()
                .userId(userId)
                .email(email)
                .name(name)
                .nickname(nickname)
                .username(username)
                .createdAt(createdAt)
                .build();

        // Act
        EmployeeData result = EmployeeData.fromAuth0Dto(auth0Dto);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(email, result.getEmail());
        assertEquals(name, result.getName());
        assertEquals(nickname, result.getNickname());
        assertEquals(username, result.getUsername());
        assertEquals(ZonedDateTime.parse(createdAt), result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void fromAuth0Dto_shouldThrowException_whenDtoIsNull() {
        // Act & Assert
        assertThrows(NoValidDtoException.class, () -> EmployeeData.fromAuth0Dto(null));
    }
}