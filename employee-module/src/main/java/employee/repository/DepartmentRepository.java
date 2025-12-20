package employee.repository;

import employee.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Department entity with optimized queries.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    /**
     * Finds department by name.
     *
     * @param name Department name
     * @return Optional containing department if found
     */
    Optional<Department> findByName(String name);

    /**
     * Checks if a department with given name already exists.
     *
     * @param name Department name to check
     * @return true if name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Fetches all departments with their managers eagerly loaded.
     * Prevents N+1 problem when accessing manager information.
     *
     * @return List of all departments with managers
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager")
    List<Department> findAllWithManagers();

    /**
     * Fetches a single department with manager eagerly loaded.
     *
     * @param id Department ID
     * @return Optional containing department with manager
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.manager WHERE d.id = :id")
    Optional<Department> findByIdWithManager(@Param("id") UUID id);
}