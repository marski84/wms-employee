package employee.repository;

import employee.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity with optimized queries to prevent N+1 problems.
 * Uses fetch joins to eagerly load related entities when needed.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Fetches users with their associated Department and the Manager of that Department.
     * Prevents N+1 problem during DTO mapping.
     *
     * @return List of all users with their department and manager eagerly loaded
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.department d " +
            "LEFT JOIN FETCH d.manager")
    List<User> findAllWithDepartmentAndManager();

    /**
     * Optimized fetch for a single user context.
     * Loads user with department and department's manager in a single query.
     *
     * @param id User ID
     * @return Optional containing user with details, or empty if not found
     */
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.department d " +
            "LEFT JOIN FETCH d.manager " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithDepartmentAndManager(@Param("id") UUID id);

    /**
     * Finds user by email address.
     * Useful for authentication and duplicate checking.
     *
     * @param email User's email address
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with given email already exists.
     *
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);
}