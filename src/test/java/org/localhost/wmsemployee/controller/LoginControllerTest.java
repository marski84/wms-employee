package org.localhost.wmsemployee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.EmailLoginRequest;
import org.localhost.wmsemployee.exceptions.AuthenticationFailedException;
import org.localhost.wmsemployee.service.login.LoginService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

    private ObjectMapper objectMapper;
    private EmailLoginRequest validEmailLoginRequest;
    private Auth0UserDto mockUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController)
                .setControllerAdvice(new org.localhost.wmsemployee.exceptions.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Setup valid login request
        validEmailLoginRequest = new EmailLoginRequest();
        validEmailLoginRequest.setEmail("test@example.com");
        validEmailLoginRequest.setPassword("Password123!");

        // Setup mock user response
        Auth0UserDto.UserMetadata userMetadata = Auth0UserDto.UserMetadata.builder()
                .phoneNumber("+1234567890")
                .address("123 Main St")
                .city("Anytown")
                .postalCode("12345")
                .country("USA")
                .roleId("1")
                .roleName(org.localhost.wmsemployee.model.eumeration.EmployeeRole.EMPLOYEE)
                .familyName("Doe")
                .build();

        mockUserResponse = Auth0UserDto.builder()
                .userId("auth0|123456789")
                .email("test@example.com")
                .name("John Doe")
                .nickname("John")
                .username("john.doe")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .userMetadata(userMetadata)
                .build();
    }

    @Test
    void login_shouldReturnUserDetails_whenLoginIsSuccessful() throws Exception {
        // Arrange
        when(loginService.handleLogin(eq("test@example.com"), eq("Password123!")))
                .thenReturn(mockUserResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id", is("auth0|123456789")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.nickname", is("John")))
                .andExpect(jsonPath("$.username", is("john.doe")));

        // Verify
        verify(loginService).handleLogin(eq("test@example.com"), eq("Password123!"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        when(loginService.handleLogin(anyString(), anyString()))
                .thenThrow(new AuthenticationFailedException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid credentials")));

        // Verify
        verify(loginService).handleLogin(eq("test@example.com"), eq("Password123!"));
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        // Arrange - create request with invalid email
        EmailLoginRequest invalidEmailRequest = new EmailLoginRequest();
        invalidEmailRequest.setEmail("invalid-email"); // Invalid email format
        invalidEmailRequest.setPassword("Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(loginService, never()).handleLogin(anyString(), anyString());
    }

    @Test
    void login_shouldReturnBadRequest_whenEmailIsMissing() throws Exception {
        // Arrange - create request with missing email
        EmailLoginRequest missingEmailRequest = new EmailLoginRequest();
        missingEmailRequest.setPassword("Password123!");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(loginService, never()).handleLogin(anyString(), anyString());
    }

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsMissing() throws Exception {
        // Arrange - create request with missing password
        EmailLoginRequest missingPasswordRequest = new EmailLoginRequest();
        missingPasswordRequest.setEmail("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(loginService, never()).handleLogin(anyString(), anyString());
    }
}
