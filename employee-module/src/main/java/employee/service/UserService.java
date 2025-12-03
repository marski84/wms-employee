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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing User CRUD operations.
 * Handles business logic, validation, and password hashing.
 */

@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with hashed password.
     *
     * @param dto User creation data
     * @return Created user DTO
     * @throws EmailAlreadyExistsException if email is already registered
     * @throws DepartmentNotFoundException if department ID is provided but not found
     */
    @Transactional
    public UserDto createUser(CreateUserDto dto) {
        log.info("Creating new user with email: {}", dto.email());

        // Check if email already exists
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Attempt to create user with existing email: {}", dto.email());
            throw new EmailAlreadyExistsException(dto.email());
        }

        // Validate and fetch department if provided
        Department department = null;
        if (dto.departmentId() != null) {
            department = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new DepartmentNotFoundException(dto.departmentId()));
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(dto.password());

        // Build user entity
        User user = User.builder()
                .name(dto.name())
                .surname(dto.surname())
                .email(dto.email())
                .phoneNumber(dto.phoneNumber())
                .password(hashedPassword)
                .jobTitle(dto.jobTitle())
                .role(dto.role() != null ? dto.role() : EmployeeRole.EMPLOYEE)
                .status(dto.status() != null ? dto.status() : EmployeeStatus.AVAILABLE)
                .department(department)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return mapToDto(savedUser);
    }

    /**
     * Retrieves all users with their department details.
     *
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAllWithDetails().stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId User ID
     * @return User DTO
     * @throws UserNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        log.debug("Fetching user with ID: {}", userId);
        User user = userRepository.findByIdWithDetails(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return mapToDto(user);
    }

    /**
     * Updates an existing user.
     * Only non-null fields in the DTO will be updated.
     *
     * @param userId User ID
     * @param dto    Update data
     * @return Updated user DTO
     * @throws UserNotFoundException       if user not found
     * @throws EmailAlreadyExistsException if new email already exists
     * @throws DepartmentNotFoundException if department ID is provided but not found
     */
    @Transactional
    public UserDto updateUser(UUID userId, UpdateUserDto dto) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Update name if provided
        if (dto.name() != null) {
            user.setName(dto.name());
        }

        // Update surname if provided
        if (dto.surname() != null) {
            user.setSurname(dto.surname());
        }

        // Update email if provided and different
        if (dto.email() != null && !dto.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.email())) {
                throw new EmailAlreadyExistsException(dto.email());
            }
            user.setEmail(dto.email());
        }

        // Update phone number if provided
        if (dto.phoneNumber() != null) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        // Update job title if provided
        if (dto.jobTitle() != null) {
            user.setJobTitle(dto.jobTitle());
        }

        // Update role if provided
        if (dto.role() != null) {
            user.setRole(dto.role());
        }

        // Update status if provided
        if (dto.status() != null) {
            user.setStatus(dto.status());
        }

        // Update department if provided
        if (dto.departmentId() != null) {
            Department department = departmentRepository.findById(dto.departmentId())
                    .orElseThrow(() -> new DepartmentNotFoundException(dto.departmentId()));
            user.setDepartment(department);
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);

        return mapToDto(updatedUser);
    }

    /**
     * Deletes a user by ID.
     *
     * @param userId User ID
     * @throws UserNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    /**
     * Maps User entity to UserDto.
     * Excludes password hash for security.
     */
    private UserDto mapToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getJobTitle(),
                user.getRole(),
                user.getStatus(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}