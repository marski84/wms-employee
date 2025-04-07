package org.localhost.wmsemployee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BasicEmployeeDataDto {
    @NotNull
    protected Long employeeId;
    @NotNull
    protected Long managerId;

    public BasicEmployeeDataDto(Long employeeId, Long managerId) {
        this.employeeId = employeeId;
        this.managerId = managerId;
    }
}
