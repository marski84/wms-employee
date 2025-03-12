package org.localhost.wmsemployee.dto;

import lombok.Getter;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;

@Getter
public class UpdateEmployeeDataAndRoleDto extends UpdateEmployeeDto {
    private EmployeeRole role;

    public UpdateEmployeeDataAndRoleDto(UpdateEmployeeDto updateEmployeeDto, EmployeeRole employeeRole) {
        role = employeeRole;
    }

}
