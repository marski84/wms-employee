package org.localhost.wmsemployee.repository.impl;

import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeQuery.EmployeeQueryRepository;
import org.localhost.wmsemployee.repository.crud.EmployeeCrudRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.StreamSupport;

@Repository
public class EmployeeQueryRepositoryImpl implements EmployeeQueryRepository {

    private static final Logger log = LoggerFactory.getLogger(EmployeeQueryRepositoryImpl.class);
    private final EmployeeCrudRepository employeeCrudRepository;

    public EmployeeQueryRepositoryImpl(EmployeeCrudRepository employeeCrudRepository) {
        this.employeeCrudRepository = employeeCrudRepository;
    }

    @Override
    public Employee getEmployeeById(Long employeeId) {
        log.warn("EmployeeQueryRepositoryImpl.getEmployeeById, employee not found: {}", employeeId);
        return employeeCrudRepository.findById(employeeId).orElseThrow(EmployeeNotFoundException::new);
    }

    @Override
    public List<Employee> getAllEmployees() {
        Iterable<Employee> employees = employeeCrudRepository.findAll();
        return StreamSupport.stream(employees.spliterator(), false)
                .toList();
    }

    @Override
    public List<Employee> getAllEmployeesBySupervisorId(Long supervisorId) {
        return getAllEmployees().stream()
                .filter(employee -> employee.getSupervisorId().equals(supervisorId))
                .toList();
    }

    @Override
    public List<Employee> getAllEmployeesByEmployeeStatus(EmployeeStatus employeeStatus) {
        return getAllEmployees().stream()
                .filter(employee -> employee.getEmployeeStatus().equals(employeeStatus))
                .toList();
    }

    @Override
    public List<Employee> getAllEmployeesByEmployeeRole(EmployeeRole employeeRole) {
        return getAllEmployees().stream()
                .filter(employee -> employee.getEmployeeRole().equals(employeeRole))
                .toList();
    }
}
