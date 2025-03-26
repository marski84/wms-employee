package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.service.EmployeeQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeQueryServiceImpl implements EmployeeQueryService {
    @Override
    public EmployeeDataDto getEmployeeById(Long id) {
        return null;
    }

    @Override
    public List<EmployeeDataDto> getEmployees() {
        return List.of();
    }

    @Override
    public List<EmployeeDataDto> getEmployeesBySupervisor(Long departmentId) {
        return List.of();
    }

    @Override
    public List<EmployeeDataDto> getEmployeesByEmployeeRole(EmployeeRole employeeRole) {
        return List.of();
    }

    @Override
    public List<EmployeeDataDto> getEmployeeByStatus(EmployeeStatus employeeStatus) {
        return List.of();
    }
}
