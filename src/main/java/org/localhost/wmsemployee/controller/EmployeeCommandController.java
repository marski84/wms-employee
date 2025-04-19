package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.*;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.service.EmployeeCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
@Validated
public class EmployeeCommandController {

    private final EmployeeCommandService employeeCommandService;

    public EmployeeCommandController(EmployeeCommandService employeeCommandService) {
        this.employeeCommandService = employeeCommandService;
    }

    @PostMapping
    public ResponseEntity<EmployeeDataDto> createEmployee(@Valid @RequestBody EmployeeRegistrationDto employeeData) {
        Employee employee = employeeCommandService.registerNewEmployee(employeeData);
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }

    @PutMapping
    public ResponseEntity<EmployeeDataDto> updateEmployee(@Valid @RequestBody UpdateEmployeeDto employeeData) {
        Employee employee = employeeCommandService.updateEmployee(employeeData);
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }

    @PutMapping("/supervisor/{supervisorId}")
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
