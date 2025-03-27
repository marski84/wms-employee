package org.localhost.wmsemployee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;

@Getter
public class EmployeeRoleUpdateDto extends BasicEmployeeDataDto {
    @NotNull
    private final EmployeeRole role;

    EmployeeRoleUpdateDto(Long employeeId, Long managerId, EmployeeRole role) {
        super(employeeId, managerId);
        this.role = role;
    }
}
