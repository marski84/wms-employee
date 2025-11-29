package employee.service;

import employee.dto.CreateDepartmentDto;
import employee.dto.DepartmentDto;
import employee.dto.UpdateDepartmentDto;
import employee.exception.DepartmentNameAlreadyExistsException;
import employee.exception.DepartmentNotFoundException;
import employee.exception.UserNotFoundException;
import employee.model.Department;
import employee.model.User;
import employee.repository.DepartmentRepository;
import employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing Department CRUD operations.
 * Handles business logic and validation for departments.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new department.
     *
     * @param dto Department creation data
     * @return Created department DTO
     * @throws DepartmentNameAlreadyExistsException if department name already exists
     * @throws UserNotFoundException                if manager ID is provided but not found
     */
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentDto dto) {
        log.info("Creating new department with name: {}", dto.name());

        // Check if department name already exists
        if (departmentRepository.existsByName(dto.name())) {
            log.warn("Attempt to create department with existing name: {}", dto.name());
            throw new DepartmentNameAlreadyExistsException(dto.name());
        }

        // Validate and fetch manager if provided
        User manager = null;
        if (dto.managerId() != null) {
            manager = userRepository.findById(dto.managerId())
                    .orElseThrow(() -> new UserNotFoundException(dto.managerId()));
        }

        // Build department entity
        Department department = Department.builder()
                .name(dto.name())
                .manager(manager)
                .build();

        Department savedDepartment = departmentRepository.save(department);
        log.info("Department created successfully with ID: {}", savedDepartment.getId());

        return mapToDto(savedDepartment);
    }

    /**
     * Retrieves all departments with their manager details.
     *
     * @return List of all departments
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        log.debug("Fetching all departments");
        return departmentRepository.findAllWithManagers().stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Retrieves a department by ID.
     *
     * @param departmentId Department ID
     * @return Department DTO
     * @throws DepartmentNotFoundException if department not found
     */
    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(UUID departmentId) {
        log.debug("Fetching department with ID: {}", departmentId);
        Department department = departmentRepository.findByIdWithManager(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        return mapToDto(department);
    }

    /**
     * Updates an existing department.
     * Only non-null fields in the DTO will be updated.
     *
     * @param departmentId Department ID
     * @param dto          Update data
     * @return Updated department DTO
     * @throws DepartmentNotFoundException          if department not found
     * @throws DepartmentNameAlreadyExistsException if new name already exists
     * @throws UserNotFoundException                if manager ID is provided but not found
     */
    @Transactional
    public DepartmentDto updateDepartment(UUID departmentId, UpdateDepartmentDto dto) {
        log.info("Updating department with ID: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));

        // Update name if provided and different
        if (dto.name() != null && !dto.name().equals(department.getName())) {
            if (departmentRepository.existsByName(dto.name())) {
                throw new DepartmentNameAlreadyExistsException(dto.name());
            }
            department.setName(dto.name());
        }

        // Update manager if provided
        if (dto.managerId() != null) {
            User manager = userRepository.findById(dto.managerId())
                    .orElseThrow(() -> new UserNotFoundException(dto.managerId()));
            department.setManager(manager);
        }

        Department updatedDepartment = departmentRepository.save(department);
        log.info("Department updated successfully: {}", departmentId);

        return mapToDto(updatedDepartment);
    }

    /**
     * Deletes a department by ID.
     * Note: Due to ON DELETE SET NULL constraint, users in this department
     * will have their department_id set to NULL.
     *
     * @param departmentId Department ID
     * @throws DepartmentNotFoundException if department not found
     */
    @Transactional
    public void deleteDepartment(UUID departmentId) {
        log.info("Deleting department with ID: {}", departmentId);

        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException(departmentId);
        }

        departmentRepository.deleteById(departmentId);
        log.info("Department deleted successfully: {}", departmentId);
    }

    /**
     * Maps Department entity to DepartmentDto.
     */
    private DepartmentDto mapToDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getName(),
                department.getManager() != null ? department.getManager().getId() : null,
                department.getManager() != null ?
                        department.getManager().getName() + " " + department.getManager().getSurname() : null
        );
    }
}