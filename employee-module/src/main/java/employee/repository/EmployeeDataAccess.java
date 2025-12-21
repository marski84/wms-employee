package employee.repository;

import employee.exception.DepartmentNotFoundException;
import employee.exception.UserNotFoundException;
import employee.model.Department;
import employee.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data Access Facade for Employee module.
 * Provides unified access to User and Department repositories,
 * reducing coupling between services and repositories.
 * <p>
 * Benefits:
 * - Single point of contact for all employee data operations
 * - Centralizes "find or throw" validation logic
 * - Services depend on one component instead of multiple repositories
 * - Easy to add cross-cutting concerns (caching, auditing, metrics)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeDataAccess {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // ==================== User Operations ====================

    /**
     * Saves a user entity.
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Finds a user by ID.
     */
    public Optional<User> findUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    /**
     * Finds a user by ID with department and manager eagerly loaded.
     * Prevents N+1 queries when accessing related entities.
     */
    public Optional<User> findUserByIdWithDepartmentAndManager(UUID userId) {
        return userRepository.findByIdWithDepartmentAndManager(userId);
    }

    /**
     * Finds all users with department and manager eagerly loaded.
     */
    public List<User> findAllUsersWithDepartmentAndManager() {
        return userRepository.findAllWithDepartmentAndManager();
    }

    /**
     * Finds a user by email.
     */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Checks if a user with given email exists.
     */
    public boolean userEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Checks if a user with given ID exists.
     */
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Deletes a user by ID.
     */
    public void deleteUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Finds a user by ID or throws UserNotFoundException.
     * Convenience method for validation scenarios.
     */
    public User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Finds a user by ID with department and manager, or throws UserNotFoundException.
     */
    public User getUserWithDepartmentOrThrow(UUID userId) {
        return userRepository.findByIdWithDepartmentAndManager(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    // ==================== Department Operations ====================

    /**
     * Saves a department entity.
     */
    public Department saveDepartment(Department department) {
        return departmentRepository.save(department);
    }

    /**
     * Finds a department by ID.
     */
    public Optional<Department> findDepartmentById(UUID departmentId) {
        return departmentRepository.findById(departmentId);
    }

    /**
     * Finds a department by ID with manager eagerly loaded.
     */
    public Optional<Department> findDepartmentByIdWithManager(UUID departmentId) {
        return departmentRepository.findByIdWithManager(departmentId);
    }

    /**
     * Finds all departments with managers eagerly loaded.
     */
    public List<Department> findAllDepartmentsWithManagers() {
        return departmentRepository.findAllWithManagers();
    }

    /**
     * Finds a department by name.
     */
    public Optional<Department> findDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }

    /**
     * Checks if a department with given name exists.
     */
    public boolean departmentNameExists(String name) {
        return departmentRepository.existsByName(name);
    }

    /**
     * Checks if a department with given ID exists.
     */
    public boolean departmentExists(UUID departmentId) {
        return departmentRepository.existsById(departmentId);
    }

    /**
     * Deletes a department by ID.
     */
    public void deleteDepartment(UUID departmentId) {
        departmentRepository.deleteById(departmentId);
    }

    /**
     * Finds a department by ID or throws DepartmentNotFoundException.
     * Convenience method for validation scenarios.
     */
    public Department getDepartmentOrThrow(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    /**
     * Finds a department by ID with manager, or throws DepartmentNotFoundException.
     */
    public Department getDepartmentWithManagerOrThrow(UUID departmentId) {
        return departmentRepository.findByIdWithManager(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }
}