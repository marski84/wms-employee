package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.EmployeeContactDetails;
import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.repository.EmployeeRepository;
import org.localhost.wmsemployee.service.EmployeeCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeCommandServiceImpl implements EmployeeCommandService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeCommandServiceImpl.class);
    private final EmployeeRepository employeeRepositoryImpl;

    public EmployeeCommandServiceImpl(EmployeeRepository employeeRepositoryImpl) {
        this.employeeRepositoryImpl = employeeRepositoryImpl;
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

        return employeeRepositoryImpl.save(newEmployee);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployee(UpdateEmployeeDto updateEmployeeDto) {
        return employeeRepositoryImpl.updateEmployeeData(updateEmployeeDto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto employee, EmployeeRole role, Long supervisorId) {
        return employeeRepositoryImpl.updateEmployeeDataBySupervisor(employee, supervisorId, role);
    }
}
