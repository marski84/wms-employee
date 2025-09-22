package org.localhost.wmsemployee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.registration.EmployeeRegistrationDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class EmployeeCommandControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmployeeCommandService employeeCommandService;

    @InjectMocks
    private EmployeeCommandController employeeCommandController;

    private ObjectMapper objectMapper;
    private EmployeeRegistrationDto validRegistrationDto;
    private Auth0RegistrationDto expectedAuth0Response;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeCommandController).build();
        objectMapper = new ObjectMapper();

        // Setup valid registration DTO
        validRegistrationDto = EmployeeRegistrationDto.builder()
                .supervisorId(1L)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .password("Password1!")
                .confirmPassword("Password1!")
                .employeeRole(EmployeeRole.EMPLOYEE)
                .address("123 Main St")
                .city("Anytown")
                .postalCode("12345")
                .country("USA")
                .build();

        // Setup expected Auth0 response
        expectedAuth0Response = Auth0RegistrationDto.builder()
                .userId("auth0|123456789")
                .email("john.doe@example.com")
                .name("John Doe")
                .nickname("John")
                .username("john.doe")
                .createdAt("2023-01-01T00:00:00.000Z")
                .build();
    }

    @Test
    void registerEmployee_shouldReturnAuth0RegistrationDto_whenRegistrationIsSuccessful() throws Exception {
        // Arrange
        when(employeeCommandService.registerEmployee(any(EmployeeRegistrationDto.class)))
                .thenReturn(expectedAuth0Response);

        // Act & Assert
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.user_id", is("auth0|123456789")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.nickname", is("John")))
                .andExpect(jsonPath("$.username", is("john.doe")))
                .andExpect(jsonPath("$.created_at", is("2023-01-01T00:00:00.000Z")));

        // Verify
        verify(employeeCommandService).registerEmployee(any(EmployeeRegistrationDto.class));
    }

    @Test
    void registerEmployee_shouldReturnBadRequest_whenValidationFails() throws Exception {
        // Arrange - create invalid DTO (missing required fields)
        EmployeeRegistrationDto invalidDto = EmployeeRegistrationDto.builder()
                .supervisorId(1L)
                // Missing name
                .surname("Doe")
                .email("john.doe@example.com")
                // Missing phone number
                .password("Password1!")
                .confirmPassword("Password1!")
                .employeeRole(EmployeeRole.EMPLOYEE)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(employeeCommandService, never()).registerEmployee(any(EmployeeRegistrationDto.class));
    }

    @Test
    void registerEmployee_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
        // Arrange - create DTO with invalid email
        EmployeeRegistrationDto invalidEmailDto = EmployeeRegistrationDto.builder()
                .supervisorId(1L)
                .name("John")
                .surname("Doe")
                .email("invalid-email") // Invalid email format
                .phoneNumber("+1234567890")
                .password("Password1!")
                .confirmPassword("Password1!")
                .employeeRole(EmployeeRole.EMPLOYEE)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(employeeCommandService, never()).registerEmployee(any(EmployeeRegistrationDto.class));
    }

    @Test
    void registerEmployee_shouldReturnBadRequest_whenPasswordIsInvalid() throws Exception {
        // Arrange - create DTO with invalid password
        EmployeeRegistrationDto invalidPasswordDto = EmployeeRegistrationDto.builder()
                .supervisorId(1L)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1234567890")
                .password("weak") // Invalid password (too simple)
                .confirmPassword("weak")
                .employeeRole(EmployeeRole.EMPLOYEE)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPasswordDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()));

        // Verify
        verify(employeeCommandService, never()).registerEmployee(any(EmployeeRegistrationDto.class));
    }
}
