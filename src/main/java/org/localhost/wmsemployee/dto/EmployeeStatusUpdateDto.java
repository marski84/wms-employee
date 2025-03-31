package org.localhost.wmsemployee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

@Getter
public class EmployeeStatusUpdateDto extends BasicEmployeeDataDto {
    @NotNull
    private final EmployeeStatus status;

    EmployeeStatusUpdateDto(Long employeeId, Long managerId, EmployeeStatus status) {
        super(employeeId, managerId);
        this.status = status;
    }
}
