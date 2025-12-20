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
import employee.repository.DepartmentRepository;
import employee.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

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
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
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
        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(false);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(testDepartment));
        when(passwordEncoder.encode(createUserDto.password())).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

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
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("hashedPassword123");
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email exists")
    void testCreateUser_EmailExists() {
        // Given
        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("john.doe@example.com");

        // Verify user was not saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DepartmentNotFoundException when department does not exist")
    void testCreateUser_DepartmentNotFound() {
        // Given
        when(userRepository.existsByEmail(createUserDto.email())).thenReturn(false);
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserDto))
                .isInstanceOf(DepartmentNotFoundException.class)
                .hasMessageContaining(departmentId.toString());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById_Success() {
        // Given
        when(userRepository.findByIdWithDetails(userId)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo("john.doe@example.com");
        verify(userRepository, times(1)).findByIdWithDetails(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user not found")
    void testGetUserById_NotFound() {
        // Given
        when(userRepository.findByIdWithDetails(userId)).thenReturn(Optional.empty());

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

        when(userRepository.findAllWithDetails()).thenReturn(List.of(testUser, user2));

        // When
        List<UserDto> results = userService.getAllUsers();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).email()).isEqualTo("john.doe@example.com");
        assertThat(results.get(1).email()).isEqualTo("jane.smith@example.com");
        verify(userRepository, times(1)).findAllWithDetails();
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

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getName()).isEqualTo("Johnny");
        assertThat(testUser.getJobTitle()).isEqualTo("Senior Software Engineer");
        assertThat(testUser.getRole()).isEqualTo(EmployeeRole.MANAGER);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void testDeleteUser_NotFound() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(userId.toString());

        verify(userRepository, never()).deleteById(any());
    }
}