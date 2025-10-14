package org.localhost.wmsemployee.controller;

import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.service.employee.EmployeeQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
public class EmployeeQueryController {
    private final EmployeeQueryService employeeQueryService;

    public EmployeeQueryController(EmployeeQueryService employeeQueryService) {
        this.employeeQueryService = employeeQueryService;
    }

    /**
     * Retrieves the details of an employee by their user ID.
     * This endpoint requires an access token with the "read:users" scope.
     *
     * @param userId The ID of the user.
     * @param accessToken The access token required to authenticate the request.
     * @return The details of the employee with the given user ID.
     */
    @GetMapping("/details/{userId}")
    public Auth0UserDto getUserDetailsByUserId(@PathVariable String userId, @RequestHeader("Authorization") String accessToken) {
        return employeeQueryService.getEmployeeDetailsByUserId(userId, accessToken);
    }

    /**
     * Retrieves the details of an employee by their user ID.
     * This endpoint requires an access token with the "read:users" scope.
     *
     * @param userId The ID of the user.
     * @return The details of the employee with the given user ID.
     * @throws SecurityException if the access token is missing or invalid.
     */
    @GetMapping("/admin/details/{userId}")
    public Object getUserDetailsByUserId(@PathVariable String userId) {
        return employeeQueryService.getEmployeeDetailsByUserId(userId);
    }

    /**
     * Retrieves employee details by username.
     *
     * @param username The username of the employee to look up
     * @return Auth0UserDto containing the employee details
     */
    @GetMapping("/by-username/{username}")
    public Auth0UserDto getUserDetailsByUsername(@PathVariable String username) {
        return employeeQueryService.getEmployeeDetailsByUsername(username);
    }
}
