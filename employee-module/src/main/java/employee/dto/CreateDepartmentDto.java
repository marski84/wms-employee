package employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for creating a new department.
 */
public record CreateDepartmentDto(
        @NotBlank(message = "Department name cannot be blank")
        @Size(max = 255, message = "Department name must not exceed 255 characters")
        String name,

        UUID managerId
) {
}