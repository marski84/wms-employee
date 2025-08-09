package org.localhost.wmsemployee.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.controller.LoginController;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.dto.login.EmailLoginRequest;
import org.localhost.wmsemployee.exceptions.AuthenticationFailedException;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.service.login.LoginService;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@ExtendWith(MockitoExtension.class)
public class LoginIntegrationTest {

    private final String testEmail = "test@example.com";
    private final String testPassword = "Password123!";
    private final String testAuthToken = "test-auth-token";
    private final String testManagementToken = "test-management-token";
    private final String testUserId = "auth0|123456789";
    @MockBean
    private LoginService loginService;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;
    private EmailLoginRequest validEmailLoginRequest;
    private Auth0UserDto mockUserResponse;

    @BeforeEach
    void setUp() {
        // Setup valid login request
        validEmailLoginRequest = new EmailLoginRequest();
        validEmailLoginRequest.setEmail(testEmail);
        validEmailLoginRequest.setPassword(testPassword);

        // Setup mock user response
        Auth0UserDto.UserMetadata userMetadata = Auth0UserDto.UserMetadata.builder()
                .phoneNumber("+1234567890")
                .address("123 Main St")
                .city("Anytown")
                .postalCode("12345")
                .country("USA")
                .roleId("1")
                .roleName(EmployeeRole.EMPLOYEE)
                .familyName("Doe")
                .build();

        mockUserResponse = Auth0UserDto.builder()
                .userId(testUserId)
                .email(testEmail)
                .name("John Doe")
                .nickname("John")
                .username("john.doe")
                .createdAt(ZonedDateTime.now())
                .updatedAt(ZonedDateTime.now())
                .userMetadata(userMetadata)
                .build();
    }

    @Test
    void loginEndToEnd_shouldReturnUserDetails_whenLoginIsSuccessful() throws Exception {
        // Arrange
        when(loginService.handleLogin(eq(testEmail), eq(testPassword)))
                .thenReturn(mockUserResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id", is(testUserId)))
                .andExpect(jsonPath("$.email", is(testEmail)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.nickname", is("John")))
                .andExpect(jsonPath("$.username", is("john.doe")));
    }

    @Test
    void loginEndToEnd_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        when(loginService.handleLogin(eq(testEmail), eq(testPassword)))
                .thenThrow(new AuthenticationFailedException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("Invalid credentials")));
    }

    @Test
    void loginEndToEnd_shouldReturnUnauthorized_whenUserNotFound() throws Exception {
        // Arrange
        when(loginService.handleLogin(eq(testEmail), eq(testPassword)))
                .thenThrow(new AuthenticationFailedException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")))
                .andExpect(jsonPath("$.message", is("User not found")));
    }
}
