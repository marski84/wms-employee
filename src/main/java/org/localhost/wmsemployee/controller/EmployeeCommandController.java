package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.EmployeeStatusUpdateDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
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

    @PatchMapping("/updateStatus")
    public ResponseEntity<EmployeeDataDto> updateEmployeeStatus(@RequestBody EmployeeStatusUpdateDto employeeStatusUpdateDto) {
        Employee employee = employeeCommandService.updateEmployeeStatusByEmployee(employeeStatusUpdateDto.getEmployeeId(), employeeStatusUpdateDto.getStatus());
        return ResponseEntity.ok().body(EmployeeDataDto.fromEmployee(employee));
    }

}
