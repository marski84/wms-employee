package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.service.auth.model.EmployeeAuthDataDto;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
@Validated
public class EmployeeCommandController {
    private final EmployeeCommandService employeeCommandService;

    public EmployeeCommandController(EmployeeCommandService employeeCommandService) {
        this.employeeCommandService = employeeCommandService;
    }

    @PostMapping
    EmployeeAuthDataDto employeeAuthDataDto(@RequestBody @Valid EmployeeRegistrationDto employeeAuthDataDto) {
        return employeeCommandService.registerEmployee(employeeAuthDataDto);
    }
}
