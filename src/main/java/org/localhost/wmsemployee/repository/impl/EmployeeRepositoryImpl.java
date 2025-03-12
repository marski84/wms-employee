package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.exceptions.NotAValidSupervisorException;
import org.localhost.wmsemployee.mappers.FromUpdateDtoToEmployeeMapper;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.dto.UpdateEmployeeDataAndRoleDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.repository.EmployeeRepository;
import org.localhost.wmsemployee.repository.crud.EmployeeSqlRepository;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static org.localhost.wmsemployee.constants.Constants.ADMIN_USER_ID;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {
    private final EmployeeSqlRepository employeeSqlRepository;

    public EmployeeRepositoryImpl(EmployeeSqlRepository employeeSqlRepository) {
        this.employeeSqlRepository = employeeSqlRepository;
    }

    public Employee getEmployeeById(Long employeeId) {
        return employeeSqlRepository.findById(employeeId).orElseThrow(
                EmployeeNotFoundException::new
        );
    }

    public Employee save(Employee employee) {
        return employeeSqlRepository.save(employee);
    }

    public Employee updateEmployeeData(UpdateEmployeeDto updateEmployeeDto) {
        Employee employee = getEmployeeById(updateEmployeeDto.getEmployeeId());

        Employee updatedEmployee = FromUpdateDtoToEmployeeMapper.mapFromUpdateDtoToEmployee(updateEmployeeDto, employee);

        return employeeSqlRepository.save(updatedEmployee);
    }

    public Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto updateEmployeeDto, Long supervisorId, EmployeeRole newEmployeeRole) {
        Employee employee = getEmployeeById(updateEmployeeDto.getEmployeeId());

        if (!Objects.equals(employee.getSupervisorId(), supervisorId) || !Objects.equals(employee.getSupervisorId(), ADMIN_USER_ID)) {
            throw new NotAValidSupervisorException();
        }

        UpdateEmployeeDataAndRoleDto updateEmployeeDataAndRoleDto = new UpdateEmployeeDataAndRoleDto(updateEmployeeDto, newEmployeeRole);

        Employee updatedEmployee = FromUpdateDtoToEmployeeMapper.mapFromUpdateDtoToEmployee(updateEmployeeDto, employee);
        updatedEmployee.setEmployeeRole(newEmployeeRole);
        return employeeSqlRepository.save(updatedEmployee);
    }



}
