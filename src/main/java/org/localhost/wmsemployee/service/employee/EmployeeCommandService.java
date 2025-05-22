package org.localhost.wmsemployee.service.employee;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.springframework.validation.annotation.Validated;

@Validated
public interface EmployeeCommandService {
        Employee registerNewEmployee(@Valid EmployeeRegistrationDto employee);
        Employee updateEmployee(@Valid UpdateEmployeeDto employee);

        Employee updateEmployeeDataBySupervisor(@Valid UpdateEmployeeDto employee, Long supervisorId);

        Employee updateEmployeeStatusBySupervisor(Long employeeId, EmployeeStatus employeeStatus, Long supervisorId);

        Employee updateEmployeeRoleBySupervisor(Long employeeId, EmployeeRole employeeRole, Long supervisorId);

        Employee updateEmployeeStatusByEmployee(Long employeeId, EmployeeStatus employeeStatus);
}

