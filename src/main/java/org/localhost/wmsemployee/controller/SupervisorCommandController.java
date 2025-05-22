package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.dto.EmployeeRoleUpdateDto;
import org.localhost.wmsemployee.dto.EmployeeStatusUpdateDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supervisor")
@Validated
public class SupervisorCommandController {
    private final EmployeeCommandService employeeCommandService;

    public SupervisorCommandController(EmployeeCommandService employeeCommandService) {
        this.employeeCommandService = employeeCommandService;
    }

    @PutMapping("/{supervisorId}")
    public ResponseEntity<EmployeeDataDto> updateEmployeeDataBySupervisor(
            @Valid @RequestBody UpdateEmployeeDto employeeData,
            @PathVariable Long supervisorId) {
        Employee employee = employeeCommandService.updateEmployeeDataBySupervisor(
                employeeData, supervisorId);
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }

    @PatchMapping("/status")
    public ResponseEntity<EmployeeDataDto> updateEmployeeStatusBySupervisor(@Valid @RequestBody EmployeeStatusUpdateDto employeeData) {
        Employee employee = employeeCommandService.updateEmployeeStatusBySupervisor(
                employeeData.getEmployeeId(), employeeData.getStatus(), employeeData.getManagerId()
        );
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }

    @PatchMapping("/role")
    public ResponseEntity<EmployeeDataDto> updateEmployeeRoleBySupervisor(@Valid @RequestBody EmployeeRoleUpdateDto employeeData) {
        Employee employee = employeeCommandService.updateEmployeeRoleBySupervisor(employeeData.getEmployeeId(), employeeData.getRole(), employeeData.getManagerId()
        );
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }
}
