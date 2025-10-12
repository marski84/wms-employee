package org.localhost.wmsemployee.service.employee;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.localhost.wmsemployee.dto.login.Auth0UserDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeQueryServiceTest {

    @Mock(lenient = true)
    private EmployeeDataService employeeDataService;

    @Mock(lenient = true)
    private Auth0ManagementTokenService auth0ManagementTokenService;

    @Mock(lenient = true)
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeQueryService employeeQueryService;


    private final String USERS_ENDPOINT = "https://test.auth0.com/api/v2/users";

    private final String testToken = "test-token";
    private final String testUserId = "auth0|123456";
    private final String testUsername = "testuser";
    private EmployeeData testEmployee;
    private Auth0UserDto testUserDto;

    @BeforeEach
    void setUp() {
        // Setup test employee
        testEmployee = EmployeeData.builder()
                .id(1L)
                .username(testUsername)
                .userId(testUserId)
                .email("test@example.com")
                .build();

        // Setup test user DTO
        testUserDto = Auth0UserDto.builder()
                .userId(testUserId)
                .email("test@example.com")
                .name("Test User")
                .build();

    }

    @Test
    void getEmployeeDetailsByUserId_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testToken);
        when(employeeDataService.findByUsername(testEmployee.getUsername())).thenReturn(Optional.of(testEmployee));

        ResponseEntity<Auth0UserDto> responseEntity = new ResponseEntity<>(testUserDto, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)
        )).thenReturn(responseEntity);

        // When
        Auth0UserDto result = employeeQueryService.getEmployeeDetailsByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        verify(auth0ManagementTokenService, times(1)).getAccessToken();
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)
        );
    }

    @Test
    void getEmployeeDetailsByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(employeeDataService.findByUsername(testUsername))
                .thenReturn(Optional.of(testEmployee));

        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testToken);

        ResponseEntity<Auth0UserDto> responseEntity = new ResponseEntity<>(testUserDto, HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)
        )).thenReturn(responseEntity);

        // When
        Auth0UserDto result = employeeQueryService.getEmployeeDetailsByUsername(testUsername);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        verify(employeeDataService, times(1)).findByUsername(testUsername);
    }

    @Test
    void getEmployeeDetailsByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(employeeDataService.findByUsername(testUsername))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EmployeeNotFoundException.class, () -> {
            employeeQueryService.getEmployeeDetailsByUsername(testUsername);
        });
        
        verify(employeeDataService, times(1)).findByUsername(testUsername);
        verifyNoInteractions(auth0ManagementTokenService, restTemplate);
    }

    @Test
    void getEmployeeDetailsByUserId_ShouldPropagateException_WhenAuth0Fails() {
        // Given
        when(auth0ManagementTokenService.getAccessToken()).thenReturn(testToken);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Auth0UserDto.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found"));

        // When & Then
        assertThrows(HttpClientErrorException.class, () -> {
            employeeQueryService.getEmployeeDetailsByUserId(testUserId);
        });
    }
}
