package org.localhost.wmsemployee.repository.EmployeeQuery;

import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

import java.util.List;

public interface EmployeeQueryRepository {
    Employee getEmployeeById(Long employeeId);

    List<Employee> getAllEmployees();

    List<Employee> getAllEmployeesBySupervisorId(Long supervisorId);

    List<Employee> getAllEmployeesByEmployeeStatus(EmployeeStatus employeeStatus);

    List<Employee> getAllEmployeesByEmployeeRole(EmployeeRole employeeRole);
}
