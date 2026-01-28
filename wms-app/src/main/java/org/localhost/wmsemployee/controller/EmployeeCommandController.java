package org.localhost.wmsemployee.controller;

import employee.dto.CreateUserDto;
import employee.dto.UserDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.config.SecurityRole;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for employee command operations (create, update, delete).
 * All endpoints require JWT authentication.
 */
@RequestMapping("/api/employee")
@Validated
@Slf4j
@RestController
public class EmployeeCommandController {

    private final EmployeeCommandService employeeCommandService;

    public EmployeeCommandController(EmployeeCommandService employeeCommandService) {
        this.employeeCommandService = employeeCommandService;
    }

    /**
     * Registers a new employee in the system and Auth0.
     * Requires: ADMIN or SUPERVISOR role in the JWT 'user_roles' claim.
     *
     * @param createUserDto The employee registration data
     * @return UserDto containing the created user details
     */
    @PostMapping
    @PreAuthorize(SecurityRole.ADMIN_OR_SUPERVISOR)
    public UserDto registerEmployee(@RequestBody @Valid CreateUserDto createUserDto) {
        log.debug("Registering new employee with email: {}", createUserDto.email());

        var auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Request by: {}, authorities: {}", auth.getName(), auth.getAuthorities());

        return employeeCommandService.registerEmployee(createUserDto);
    }
}
