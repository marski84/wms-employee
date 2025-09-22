package org.localhost.wmsemployee.service.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.registration.EmployeeAuthDataDto;
import org.localhost.wmsemployee.dto.registration.EmployeeRegistrationDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeCommandServiceTest {

    @Mock
    private Auth0ManagementTokenService tokenService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmployeeDataService employeeDataService;

    @InjectMocks
    private EmployeeCommandService employeeCommandService;

    private EmployeeRegistrationDto validRegistrationDto;
    private Auth0RegistrationDto expectedAuth0Response;

    @BeforeEach
    void setUp() {
        // Setup valid registration DTO
        validRegistrationDto = EmployeeRegistrationDto.builder()
                .supervisorId(1L)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+1(123)456-7890")
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

        // Set the usersEndpoint field in EmployeeCommandService
        ReflectionTestUtils.setField(employeeCommandService, "usersEndpoint", "https://test.auth0.com/api/v2/users");
    }

    @Test
    void registerEmployee_shouldReturnAuth0RegistrationDto_whenRegistrationIsSuccessful() {
        // Arrange
        when(tokenService.getAccessToken()).thenReturn("mock-token");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Auth0RegistrationDto.class)))
                .thenReturn(expectedAuth0Response);
        doNothing().when(employeeDataService).save(any(Auth0RegistrationDto.class));

        // Act
        Auth0RegistrationDto result = employeeCommandService.registerEmployee(validRegistrationDto);

        // Assert
        assertNotNull(result);
        assertEquals(expectedAuth0Response.getUserId(), result.getUserId());
        assertEquals(expectedAuth0Response.getEmail(), result.getEmail());
        assertEquals(expectedAuth0Response.getName(), result.getName());
        assertEquals(expectedAuth0Response.getNickname(), result.getNickname());
        assertEquals(expectedAuth0Response.getUsername(), result.getUsername());
        assertEquals(expectedAuth0Response.getCreatedAt(), result.getCreatedAt());

        // Verify interactions
        verify(tokenService).getAccessToken();
        verify(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(Auth0RegistrationDto.class));
    }

    @Test
    void registerEmployee_shouldPassCorrectDataToAuth0_whenCalled() {
        // Arrange
        when(tokenService.getAccessToken()).thenReturn("mock-token");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Auth0RegistrationDto.class)))
                .thenReturn(expectedAuth0Response);
        doNothing().when(employeeDataService).save(any(Auth0RegistrationDto.class));

        // Act
        employeeCommandService.registerEmployee(validRegistrationDto);

        // Assert & Verify
        verify(restTemplate).postForObject(
                contains("/users"),
                argThat(entity -> {
                    HttpEntity<EmployeeAuthDataDto> httpEntity = (HttpEntity<EmployeeAuthDataDto>) entity;
                    EmployeeAuthDataDto dto = httpEntity.getBody();

                    // Verify headers
                    assertTrue(httpEntity.getHeaders().getContentType().includes(org.springframework.http.MediaType.APPLICATION_JSON));
                    assertEquals("Bearer mock-token", httpEntity.getHeaders().getFirst("Authorization"));

                    // Verify body
                    assertNotNull(dto);
                    assertEquals(validRegistrationDto.getEmail(), dto.getEmail());
                    assertEquals(validRegistrationDto.getPassword(), dto.getPassword());
                    assertEquals("Username-Password-Authentication", dto.getConnection());
                    assertTrue(dto.isEmailVerified());
                    assertEquals(validRegistrationDto.getName() + " " + validRegistrationDto.getSurname(), dto.getName());

                    return true;
                }),
                eq(Auth0RegistrationDto.class)
        );
    }

    @Test
    void registerEmployee_shouldPropagateException_whenAuth0CallFails() {
        // Arrange
        when(tokenService.getAccessToken()).thenReturn("mock-token");
        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(Auth0RegistrationDto.class)))
                .thenThrow(new RuntimeException("Auth0 API error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            employeeCommandService.registerEmployee(validRegistrationDto);
        });

        assertEquals("Auth0 API error", exception.getMessage());

        // Verify interactions
        verify(tokenService).getAccessToken();
        verify(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(Auth0RegistrationDto.class));
    }
}
