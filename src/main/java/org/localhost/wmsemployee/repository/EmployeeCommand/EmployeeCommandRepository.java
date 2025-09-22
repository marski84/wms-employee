package org.localhost.wmsemployee.repository.EmployeeCommand;

import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeCommandRepository {
    Employee getEmployeeById(Long employeeId);

    Employee save(Employee employee);

    Employee updateEmployeeData(UpdateEmployeeDto updateEmployeeDto);

    Employee updateEmployeeDataBySupervisor(UpdateEmployeeDto updateEmployeeDto, Long supervisorId);

    Employee updateEmployeeStatus(Long employeeId, EmployeeStatus newStatus, Long supervisorId);

    Employee updateEmployeeRole(Long employeeId, EmployeeRole newRole, Long supervisorId);

    Employee updateEmployeeStatusInternal(Long employeeId, EmployeeStatus newStatus);
}
