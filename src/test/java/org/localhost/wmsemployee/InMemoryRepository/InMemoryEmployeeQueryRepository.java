package org.localhost.wmsemployee.InMemoryRepository;

import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeQuery.EmployeeQueryRepository;

import java.util.List;

public class InMemoryEmployeeQueryRepository extends InMemoryEmployeeCommandRepository implements EmployeeQueryRepository {

    @Override
    public Employee getEmployeeById(Long employeeId) {
        return employees.values().stream().filter(employee -> employee.getId().equals(employeeId))
                .findFirst()
                .orElseThrow(EmployeeNotFoundException::new);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employees.values().stream().toList();
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
