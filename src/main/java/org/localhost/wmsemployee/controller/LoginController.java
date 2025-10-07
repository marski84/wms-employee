package org.localhost.wmsemployee.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.EmailLoginRequest;
import org.localhost.wmsemployee.dto.login.TokenResponseDto;
import org.localhost.wmsemployee.service.login.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling API-based user authentication operations.
 * This controller handles password grant flow for API/mobile clients.
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
     * Handles user login requests and returns user details.
     * Legacy endpoint that returns user information.
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

    /**
     * API login endpoint that returns JWT tokens for API/mobile clients.
     * This endpoint is designed for programmatic access and returns access tokens.
     *
     * @param emailLoginRequest The login request containing email and password
     * @return ResponseEntity with TokenResponseDto containing JWT tokens
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponseDto> getToken(@Valid @RequestBody EmailLoginRequest emailLoginRequest) {
        log.debug("Token request received for email: {}", emailLoginRequest.getEmail());

        TokenResponseDto tokenResponse = loginService.handleApiLogin(
                emailLoginRequest.getEmail(),
                emailLoginRequest.getPassword()
        );

        return ResponseEntity.ok(tokenResponse);
    }
}