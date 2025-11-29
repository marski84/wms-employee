package employee.dto;

import employee.dto.validation.PasswordMatches;
import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for creating a new user.
 * Includes password and confirmPassword for initial account creation.
 */
@PasswordMatches
public record CreateUserDto(
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Surname cannot be blank")
        @Size(max = 100, message = "Surname must not exceed 100 characters")
        String surname,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone number must be valid (9-15 digits, optional + prefix)")
        String phoneNumber,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Confirm password cannot be blank")
        String confirmPassword,

        @Size(max = 100, message = "Job title must not exceed 100 characters")
        String jobTitle,

        EmployeeRole role,

        EmployeeStatus status,

        UUID departmentId
) {
}