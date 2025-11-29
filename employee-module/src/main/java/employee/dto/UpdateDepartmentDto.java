package employee.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for updating an existing department.
 * All fields are optional.
 */
public record UpdateDepartmentDto(
        @Size(max = 255, message = "Department name must not exceed 255 characters")
        String name,

        UUID managerId
) {
}