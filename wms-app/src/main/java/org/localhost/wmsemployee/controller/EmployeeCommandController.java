package org.localhost.wmsemployee.controller;

import employee.dto.CreateUserDto;
import employee.dto.UserDto;
import employee.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.config.SecurityRole;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/employee")
@Validated
@Slf4j
@RestController
public class EmployeeCommandController {
    private final UserService userService;

    public EmployeeCommandController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new employee in the system and Auth0.
     * Requires: ADMIN or SUPERVISOR role in the JWT 'user_roles' claim.
     *
     * @param employeeRegistrationDto The employee registration data
     * @return Auth0RegistrationDto containing the Auth0 response with user details
     */
    @PostMapping
    @PreAuthorize(SecurityRole.ADMIN_OR_SUPERVISOR)
    UserDto registerEmployee(
            @RequestBody @Valid CreateUserDto employeeRegistrationDto
    ) {
        // Log current user's authorities for debugging
        var auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("=== Current User Authentication ===");
        log.info("Principal: {}", auth.getPrincipal());
        log.info("Authorities: {}", auth.getAuthorities());
        auth.getAuthorities().forEach(a -> log.info("  Authority: {}", a.getAuthority()));
        log.info("====================================");

        return userService.createUser(employeeRegistrationDto);
    }
}
