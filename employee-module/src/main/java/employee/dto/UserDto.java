package employee.dto;

import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for returning User data.
 * Excludes password hash for security.
 */
public record UserDto(
        UUID id,
        String name,
        String surname,
        String email,
        String phoneNumber,
        String jobTitle,
        EmployeeRole role,
        EmployeeStatus status,
        UUID departmentId,
        String departmentName,
        Instant createdAt,
        Instant updatedAt
) {
}