package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.registration.EmployeeRegistrationDto;
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

    /**
     * Registers a new employee in the system and Auth0.
     *
     * @param employeeRegistrationDto The employee registration data
     * @return Auth0RegistrationDto containing the Auth0 response with user details
     */
    @PostMapping
    Auth0RegistrationDto registerEmployee(@RequestBody @Valid EmployeeRegistrationDto employeeRegistrationDto) {
        return employeeCommandService.registerEmployee(employeeRegistrationDto);
    }
}
