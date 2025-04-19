package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeQuery.EmployeeQueryRepository;
import org.localhost.wmsemployee.service.EmployeeQueryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeQueryServiceImpl implements EmployeeQueryService {

    private final EmployeeQueryRepository employeeQueryRepository;

    public EmployeeQueryServiceImpl(EmployeeQueryRepository employeeQueryRepository) {
        this.employeeQueryRepository = employeeQueryRepository;
    }


    @Override
    public EmployeeDataDto getEmployeeById(Long id) {
        Employee employee = employeeQueryRepository.getEmployeeById(id);
        return EmployeeDataDto.fromEmployee(employee);
    }

    @Override
    public List<EmployeeDataDto> getEmployees() {
        return employeeQueryRepository.getAllEmployees().stream()
                .map(EmployeeDataDto::fromEmployee)
                .toList();
    }

    @Override
    public List<EmployeeDataDto> getEmployeesBySupervisor(Long supervisorId) {
        return employeeQueryRepository.getAllEmployeesBySupervisorId(supervisorId).stream()
                .map(EmployeeDataDto::fromEmployee)
                .toList();
    }

    @Override
    public List<EmployeeDataDto> getEmployeesByEmployeeRole(EmployeeRole employeeRole) {
        return employeeQueryRepository.getAllEmployeesByEmployeeRole(employeeRole).stream()
                .map(EmployeeDataDto::fromEmployee)
                .toList();
    }

    @Override
    public List<EmployeeDataDto> getEmployeeByStatus(EmployeeStatus employeeStatus) {
        return employeeQueryRepository.getAllEmployeesByEmployeeStatus(employeeStatus).stream()
                .map(EmployeeDataDto::fromEmployee)
                .toList();
    }
}
