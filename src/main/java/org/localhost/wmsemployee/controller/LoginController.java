package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.EmailLoginRequest;
import org.localhost.wmsemployee.service.login.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Handles user login requests.
     *
     * @param emailLoginRequest The login request containing email and password
     * @return ResponseEntity with Auth0UserDto if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<Auth0UserDto> login(@Valid @RequestBody EmailLoginRequest emailLoginRequest) {
        log.debug("Login request received for email: {}", emailLoginRequest.getEmail());

        Auth0UserDto userDto = loginService.handleLogin(
                emailLoginRequest.getEmail(),
                emailLoginRequest.getPassword()
        );

        return ResponseEntity.ok(userDto);
    }
}