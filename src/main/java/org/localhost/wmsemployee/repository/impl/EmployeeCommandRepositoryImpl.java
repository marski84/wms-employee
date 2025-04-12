package org.localhost.wmsemployee.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.exceptions.NotAValidSupervisorException;
import org.localhost.wmsemployee.mappers.FromUpdateDtoToEmployeeMapper;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeCommand.EmployeeCommandRepository;
import org.localhost.wmsemployee.repository.crud.EmployeeCrudRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Repository
@Slf4j
public class EmployeeCommandRepositoryImpl implements EmployeeCommandRepository {
    private final EmployeeCrudRepository employeeCrudRepository;

    private String adminUserId;


    public EmployeeCommandRepositoryImpl(
            EmployeeCrudRepository employeeCrudRepository,
            @Value("${ADMIN_USER_ID}") String adminUserId) {
        this.employeeCrudRepository = employeeCrudRepository;
        this.adminUserId = adminUserId;
    }

    public Employee getEmployeeById(Long employeeId) {
        log.warn("EmployeeCommandRepositoryImpl.getEmployeeById, employee not found: {}", employeeId);
        return employeeCrudRepository.findById(employeeId).orElseThrow(
                EmployeeNotFoundException::new
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee save(Employee employee) {
        return employeeCrudRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployeeData(UpdateEmployeeDto updateEmployeeDto) {
        Employee employee = getEmployeeById(updateEmployeeDto.getEmployeeId());

        Employee updatedEmployee = FromUpdateDtoToEmployeeMapper.mapFromUpdateDtoToEmployee(updateEmployeeDto, employee);

        return employeeCrudRepository.save(updatedEmployee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto updateEmployeeDto, Long supervisorId) {
        Employee employee = getEmployeeById(updateEmployeeDto.getEmployeeId());

        if (!Objects.equals(employee.getSupervisorId(), supervisorId) && !Objects.equals(employee.getSupervisorId(), adminUserId)) {
            throw new NotAValidSupervisorException();
        }

        Employee updatedEmployee = FromUpdateDtoToEmployeeMapper.mapFromUpdateDtoToEmployee(updateEmployeeDto, employee);
        return employeeCrudRepository.save(updatedEmployee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployeeStatus(Long employeeId, EmployeeStatus newStatus, Long supervisorId) {
        Employee employee = getEmployeeById(employeeId);

        if (!employee.getSupervisorId().equals(supervisorId) && !Objects.equals(employee.getSupervisorId(), adminUserId)) {
            throw new NotAValidSupervisorException();
        }
        employee.setEmployeeStatus(newStatus);
        return save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee updateEmployeeRole(Long employeeId, EmployeeRole newRole, Long supervisorId) {
        Employee employee = getEmployeeById(employeeId);

        if (!employee.getSupervisorId().equals(supervisorId) && !Objects.equals(employee.getSupervisorId(), adminUserId)) {
            throw new NotAValidSupervisorException();
        }
        employee.setEmployeeRole(newRole);
        return save(employee);
    }


}
