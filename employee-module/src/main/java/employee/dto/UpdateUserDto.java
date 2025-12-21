package employee.dto;

import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing user.
 * All fields are optional - only provided fields will be updated.
 * Password excluded - use separate endpoint for password changes.
 */
public record UpdateUserDto(
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 100, message = "Surname must not exceed 100 characters")
        String surname,

        @Email(message = "Email must be valid")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone number must be valid (9-15 digits, optional + prefix)")
        String phoneNumber,

        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        @NotNull
        EmployeeRole role,

        @NotNull
        EmployeeStatus status,

        UUID departmentId
) {
}