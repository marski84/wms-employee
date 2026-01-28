package org.localhost.wmsemployee.service.employee;

import auth.dto.registration.Auth0RegistrationDto;
import employee.dto.CreateUserDto;
import employee.dto.UserDto;
import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import employee.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeCommandService.
 * Tests the employee registration orchestration flow.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeCommandService Unit Tests")
class EmployeeCommandServiceTest {

    private static final String AUTH0_USER_ID = "auth0|abc123def456";
    @Mock
    private Auth0CommandService auth0CommandService;
    @Mock
    private UserService userService;
    @InjectMocks
    private EmployeeCommandService employeeCommandService;
    private UUID userId;
    private UUID departmentId;
    private CreateUserDto createUserDto;
    private UserDto userDto;
    private UserDto linkedUserDto;
    private Auth0RegistrationDto auth0Response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        createUserDto = new CreateUserDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Password@123",
                "Password@123",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                departmentId
        );

        userDto = new UserDto(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                departmentId,
                "IT Department"
        );

        linkedUserDto = new UserDto(
                userId,
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                departmentId,
                "IT Department"
        );

        auth0Response = new Auth0RegistrationDto(
                AUTH0_USER_ID,
                "john.doe@example.com",
                "John Doe",
                "john.doe",
                "2026-01-18T12:00:00.000Z"
        );
    }

    @Test
    @DisplayName("Should register employee successfully - full flow")
    void testRegisterEmployee_Success() {
        // Given
        when(auth0CommandService.registerInAuth0(createUserDto)).thenReturn(auth0Response);
        when(userService.createUser(createUserDto)).thenReturn(userDto);
        when(userService.setAuth0UserId(userId, AUTH0_USER_ID)).thenReturn(linkedUserDto);

        // When
        UserDto result = employeeCommandService.registerEmployee(createUserDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("john.doe@example.com");

        // Verify the flow order: Auth0 first, then local DB, then link
        var inOrder = inOrder(auth0CommandService, userService);
        inOrder.verify(auth0CommandService).registerInAuth0(createUserDto);
        inOrder.verify(userService).createUser(createUserDto);
        inOrder.verify(userService).setAuth0UserId(userId, AUTH0_USER_ID);
    }

    @Test
    @DisplayName("Should propagate exception when Auth0 registration fails")
    void testRegisterEmployee_Auth0Failure() {
        // Given
        RuntimeException auth0Exception = new RuntimeException("Auth0 API error");
        when(auth0CommandService.registerInAuth0(createUserDto)).thenThrow(auth0Exception);

        // When & Then
        assertThatThrownBy(() -> employeeCommandService.registerEmployee(createUserDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Auth0 API error");

        // Verify local DB was never called
        verify(userService, never()).createUser(any());
        verify(userService, never()).setAuth0UserId(any(), any());
    }

    @Test
    @DisplayName("Should propagate exception when local user creation fails after Auth0 success")
    void testRegisterEmployee_LocalDbFailure() {
        // Given
        when(auth0CommandService.registerInAuth0(createUserDto)).thenReturn(auth0Response);
        RuntimeException dbException = new RuntimeException("Database error");
        when(userService.createUser(createUserDto)).thenThrow(dbException);

        // When & Then
        assertThatThrownBy(() -> employeeCommandService.registerEmployee(createUserDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");

        // Verify Auth0 was called but setAuth0UserId was not
        verify(auth0CommandService).registerInAuth0(createUserDto);
        verify(userService).createUser(createUserDto);
        verify(userService, never()).setAuth0UserId(any(), any());
    }

    @Test
    @DisplayName("Should propagate exception when linking Auth0 ID fails")
    void testRegisterEmployee_LinkingFailure() {
        // Given
        when(auth0CommandService.registerInAuth0(createUserDto)).thenReturn(auth0Response);
        when(userService.createUser(createUserDto)).thenReturn(userDto);
        RuntimeException linkException = new RuntimeException("Failed to link Auth0 ID");
        when(userService.setAuth0UserId(userId, AUTH0_USER_ID)).thenThrow(linkException);

        // When & Then
        assertThatThrownBy(() -> employeeCommandService.registerEmployee(createUserDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to link Auth0 ID");

        // Verify full flow was attempted
        verify(auth0CommandService).registerInAuth0(createUserDto);
        verify(userService).createUser(createUserDto);
        verify(userService).setAuth0UserId(userId, AUTH0_USER_ID);
    }

    @Test
    @DisplayName("Should handle null Auth0 response gracefully")
    void testRegisterEmployee_NullAuth0Response() {
        // Given
        when(auth0CommandService.registerInAuth0(createUserDto)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> employeeCommandService.registerEmployee(createUserDto))
                .isInstanceOf(NullPointerException.class);

        // Verify local DB was never called due to NPE
        verify(userService, never()).createUser(any());
    }
}