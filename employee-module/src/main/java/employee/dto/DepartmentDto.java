package employee.dto;

import java.util.UUID;

/**
 * DTO for returning Department data.
 */
public record DepartmentDto(
        UUID id,
        String name,
        UUID managerId,
        String managerName
) {
}