package org.localhost.wmsemployee.controller;

import org.localhost.wmsemployee.dto.UserLoginDto;
import org.localhost.wmsemployee.service.EmployeeCredentialsQueryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final EmployeeCredentialsQueryService employeeCredentialsQueryService;

    public LoginController(EmployeeCredentialsQueryService employeeCredentialsQueryService) {
        this.employeeCredentialsQueryService = employeeCredentialsQueryService;
    }

    @PostMapping("/validate")
    public boolean validateLogin(@RequestBody UserLoginDto userLoginDto) {
        return employeeCredentialsQueryService.validateUserLogin(userLoginDto);
    }

}
