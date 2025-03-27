package org.localhost.wmsemployee.service;

import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

import java.util.List;

public interface EmployeeQueryService {
    EmployeeDataDto getEmployeeById(Long id);

    List<EmployeeDataDto> getEmployees();

    List<EmployeeDataDto> getEmployeesBySupervisor(Long departmentId);

    List<EmployeeDataDto> getEmployeesByEmployeeRole(EmployeeRole employeeRole);

    List<EmployeeDataDto> getEmployeeByStatus(EmployeeStatus employeeStatus);
}
