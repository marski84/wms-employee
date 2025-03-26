package org.localhost.wmsemployee.repository;

import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository {
    public Employee save(Employee employee);
    public Employee updateEmployeeData(UpdateEmployeeDto updateEmployeeDto);
    Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto updateEmployeeDto, Long supervisorId, EmployeeRole newEmployeeRole);
}
