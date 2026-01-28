package employee.service;

import employee.dto.CreateUserDto;
import employee.dto.UpdateUserDto;
import employee.dto.UserDto;
import employee.exception.DepartmentNotFoundException;
import employee.exception.EmailAlreadyExistsException;
import employee.exception.UserNotFoundException;
import employee.model.Department;
import employee.model.User;
import employee.model.enumeration.EmployeeRole;
import employee.model.enumeration.EmployeeStatus;
import employee.repository.EmployeeDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests business logic in isolation using mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private EmployeeDataAccess dataAccess;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UUID departmentId;
    private User testUser;
    private Department testDepartment;
    private CreateUserDto createUserDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        testDepartment = Department.builder()
                .id(departmentId)
                .name("IT Department")
                .build();

        testUser = User.builder()
                .id(userId)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+48123456789")
                .password("hashedPassword123")
                .jobTitle("Software Engineer")
                .role(EmployeeRole.EMPLOYEE)
                .status(EmployeeStatus.AVAILABLE)
                .department(testDepartment)
                .build();

        createUserDto = new CreateUserDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+48123456789",
                "password123",
                "password123",
                "Software Engineer",
                EmployeeRole.EMPLOYEE,
                EmployeeStatus.AVAILABLE,
                departmentId
        );
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser_Success() {
        // Given
        when(dataAccess.userEmailExists(createUserDto.email())).thenReturn(false);
        when(dataAccess.getDepartmentOrThrow(departmentId)).thenReturn(testDepartment);
        when(passwordEncoder.encode(createUserDto.password())).thenReturn("hashedPassword123");
        when(dataAccess.saveUser(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.createUser(createUserDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("john.doe@example.com");
        assertThat(result.name()).isEqualTo("John");
        assertThat(result.surname()).isEqualTo("Doe");
        assertThat(result.departmentId()).isEqualTo(departmentId);

        // Verify password was hashed
        verify(passwordEncoder, times(1)).encode("password123");

        // Verify user was saved
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(dataAccess, times(1)).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void testCreateUser_EmailExists() {
        // Given
        when(dataAccess.userEmailExists(createUserDto.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("john.doe@example.com");

        // Verify user was not saved
        verify(dataAccess, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Should throw DepartmentNotFoundException when department does not exist")
    void testCreateUser_DepartmentNotFound() {
        // Given
        when(dataAccess.userEmailExists(createUserDto.email())).thenReturn(false);
        when(dataAccess.getDepartmentOrThrow(departmentId)).thenThrow(new DepartmentNotFoundException(departmentId));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining(departmentId.toString());

        verify(dataAccess, never()).saveUser(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Given
        when(dataAccess.getUserWithDepartmentOrThrow(userId)).thenReturn(testUser);

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("john.doe@example.com");
        verify(dataAccess, times(1)).getUserWithDepartmentOrThrow(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void testGetUserById_NotFound() {
        // Given
        when(dataAccess.getUserWithDepartmentOrThrow(userId)).thenThrow(new UserNotFoundException(userId));

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void testGetAllUsers_Success() {
        // Given
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Jane")
                .surname("Smith")
                .email("jane.smith@example.com")
                .password("hashedPassword")
                .role(EmployeeRole.MANAGER)
                .status(EmployeeStatus.AVAILABLE)
                .build();

        when(dataAccess.findAllUsersWithDepartmentAndManager()).thenReturn(List.of(testUser, user2));

        // When
        List<UserDto> results = userService.getAllUsers();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).email()).isEqualTo("john.doe@example.com");
        assertThat(results.get(1).email()).isEqualTo("jane.smith@example.com");
        verify(dataAccess, times(1)).findAllUsersWithDepartmentAndManager();
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser_Success() {
        // Given
        UpdateUserDto updateDto = new UpdateUserDto(
                "Johnny",
                null,
                null,
                null,
                "Senior Software Engineer",
                EmployeeRole.MANAGER,
                null,
                null
        );

        when(dataAccess.getUserOrThrow(userId)).thenReturn(testUser);
        when(dataAccess.saveUser(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(dataAccess, times(1)).saveUser(testUser);
        assertThat(testUser.getName()).isEqualTo("Johnny");
        assertThat(testUser.getJobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(testUser.getRole()).isEqualTo(EmployeeRole.MANAGER);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        // Given
        when(dataAccess.userExists(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(dataAccess, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void testDeleteUser_NotFound() {
        // Given
        when(dataAccess.userExists(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(dataAccess, never()).deleteUser(any());
    }

    @Test
    @DisplayName("Should set Auth0 user ID successfully")
    void testSetAuth0UserId_Success() {
        // Given
        String auth0UserId = "auth0|abc123def456";
        when(dataAccess.getUserOrThrow(userId)).thenReturn(testUser);
        when(dataAccess.saveUser(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.setAuth0UserId(userId, auth0UserId);

        // Then
        assertThat(result).isNotNull();
        verify(dataAccess, times(1)).getUserOrThrow(userId);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(dataAccess, times(1)).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getAuthUserID()).isEqualTo(auth0UserId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when setting Auth0 ID for non-existent user")
    void testSetAuth0UserId_UserNotFound() {
        // Given
        String auth0UserId = "auth0|abc123def456";
        when(dataAccess.getUserOrThrow(userId)).thenThrow(new UserNotFoundException(userId));

        // When & Then
        assertThatThrownBy(() -> userService.setAuth0UserId(userId, auth0UserId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(dataAccess, never()).saveUser(any());
    }
}