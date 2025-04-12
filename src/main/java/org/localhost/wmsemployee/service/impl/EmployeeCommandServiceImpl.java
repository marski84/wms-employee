package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.EmployeeContactDetails;
import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeCommand.EmployeeCommandRepository;
import org.localhost.wmsemployee.service.EmployeeCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeCommandServiceImpl implements EmployeeCommandService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeCommandServiceImpl.class);
    private final EmployeeCommandRepository employeeCommandRepositoryImpl;

    public EmployeeCommandServiceImpl(EmployeeCommandRepository employeeCommandRepositoryImpl) {
        this.employeeCommandRepositoryImpl = employeeCommandRepositoryImpl;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Employee registerNewEmployee(EmployeeRegistrationDto employeeRegistrationDto) {
        Employee newEmployee = Employee.fromRegistrationDto(employeeRegistrationDto);
        log.info("New employee registered: {}", newEmployee.getSupervisorId());
        EmployeeContactDetails employeeContactDetails = EmployeeContactDetails.fromEmployeeContactDetails(employeeRegistrationDto, newEmployee);
        newEmployee.setEmployeeContactDetails(employeeContactDetails);

        EmployeeCredentials employeeCredentials = EmployeeCredentials.fromEmployee(employeeRegistrationDto, newEmployee);
        newEmployee.setCredentials(employeeCredentials);

        return employeeCommandRepositoryImpl.save(newEmployee);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    public Employee updateEmployee(UpdateEmployeeDto updateEmployeeDto) {
        return employeeCommandRepositoryImpl.updateEmployeeData(updateEmployeeDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    public Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto employee, Long supervisorId) {
        return employeeCommandRepositoryImpl.updateEmployeeDataBySupervisor(employee, supervisorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    public Employee updateEmployeeStatusBySupervisor(Long employeeId, EmployeeStatus employeeStatus, Long supervisorId) {
        return employeeCommandRepositoryImpl.updateEmployeeStatus(employeeId, employeeStatus, supervisorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    public Employee updateEmployeeRoleBySupervisor(Long employeeId, EmployeeRole employeeRole, Long supervisorId) {
        return employeeCommandRepositoryImpl.updateEmployeeRole(employeeId, employeeRole, supervisorId);
    }
}
