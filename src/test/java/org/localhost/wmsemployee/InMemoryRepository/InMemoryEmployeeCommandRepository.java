package org.localhost.wmsemployee.InMemoryRepository;

import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.exceptions.NotAValidSupervisorException;
import org.localhost.wmsemployee.mappers.FromUpdateDtoToEmployeeMapper;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeCommand.EmployeeCommandRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;


public class InMemoryEmployeeCommandRepository implements EmployeeCommandRepository {
    private final Long ADMIN_USER_ID = 999666999L;
    protected Map<Long, Employee> employees = new HashMap<>();
    protected AtomicLong idCounter = new AtomicLong(1);

    @Override
    public Employee getEmployeeById(Long employeeId) {
        return employees.values().stream()
                .filter(employee -> employee.getId().equals(employeeId))
                .findFirst()
                .orElseThrow(EmployeeNotFoundException::new);
    }

    @Override
    public Employee save(Employee employee) {
        if (employee.getId() == null) {
            Long id = idCounter.getAndIncrement();
            employee.setId(id);
            employees.put(id, employee);
        } else {
            employees.put(employee.getId(), employee);
        }
        return employees.get(employee.getId());
    }

    @Override
    public Employee updateEmployeeData(UpdateEmployeeDto updateEmployeeDto) {
        Employee employee = employees.get(updateEmployeeDto.getEmployeeId());
        if (employee == null) {
            throw new EmployeeNotFoundException();
        }
        employee.setName(updateEmployeeDto.getName());
        employee.setSurname(updateEmployeeDto.getSurname());
        employee.getEmployeeContactDetails().setEmail(updateEmployeeDto.getEmail());
        employee.getEmployeeContactDetails().setPhoneNumber(updateEmployeeDto.getPhoneNumber());


        employee.getEmployeeContactDetails().setAddress(updateEmployeeDto.getAddress());
        employee.getEmployeeContactDetails().setCity(updateEmployeeDto.getCity());
        employee.getEmployeeContactDetails().setAddress(updateEmployeeDto.getAddress());
        employee.getEmployeeContactDetails().setCountry(updateEmployeeDto.getCountry());
        employee.getEmployeeContactDetails().setPostalCode(updateEmployeeDto.getPostalCode());

        return save(employee);
    }

    @Override
    public Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto updateEmployeeDto, Long supervisorId) {
        Employee employee = employees.get(updateEmployeeDto.getEmployeeId());

        if (!Objects.equals(employee.getSupervisorId(), supervisorId) && !Objects.equals(employee.getSupervisorId(), ADMIN_USER_ID)) {
            throw new NotAValidSupervisorException();
        }

        Employee updatedEmployee = FromUpdateDtoToEmployeeMapper.mapFromUpdateDtoToEmployee(updateEmployeeDto, employee);
        return save(updatedEmployee);
    }

    @Override
    public Employee updateEmployeeStatus(Long employeeId, EmployeeStatus newStatus, Long supervisorId) {
        Employee employee = employees.get(employeeId);
        if (employee == null) {
            throw new EmployeeNotFoundException();
        }
        employee.setEmployeeStatus(newStatus);
        return save(employee);
    }

    @Override
    public Employee updateEmployeeRole(Long employeeId, EmployeeRole newRole, Long supervisorId) {
        Employee employee = employees.get(employeeId);
        if (employee == null) {
            throw new EmployeeNotFoundException();
        }
        employee.setEmployeeRole(newRole);
        return save(employee);
    }
}
