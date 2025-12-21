package employee.controller;

import employee.dto.CreateDepartmentDto;
import employee.dto.DepartmentDto;
import employee.dto.UpdateDepartmentDto;
import employee.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Department CRUD operations.
 * Handles HTTP requests and delegates business logic to DepartmentService.
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Slf4j
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * Creates a new department.
     *
     * @param dto Department creation data (validated)
     * @return Created department DTO with 201 CREATED status
     */
    @PostMapping
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody CreateDepartmentDto dto) {
        log.info("POST /api/departments - Creating department with data: {}", dto);
        DepartmentDto createdDepartment = departmentService.createDepartment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDepartment);
    }

    /**
     * Retrieves all departments.
     *
     * @return List of all departments with 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        log.info("GET /api/departments - Fetching all departments");
        List<DepartmentDto> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * Retrieves a specific department by ID.
     *
     * @param departmentId Department ID
     * @return Department DTO with 200 OK status
     */
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable UUID departmentId) {
        log.info("GET /api/departments/{} - Fetching department", departmentId);
        DepartmentDto department = departmentService.getDepartmentById(departmentId);
        return ResponseEntity.ok(department);
    }

    /**
     * Updates an existing department.
     *
     * @param departmentId Department ID
     * @param dto          Update data (validated)
     * @return Updated department DTO with 200 OK status
     */
    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentDto> updateDepartment(
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentDto dto) {
        log.info("PUT /api/departments/{} - Updating department", departmentId);
        DepartmentDto updatedDepartment = departmentService.updateDepartment(departmentId, dto);
        return ResponseEntity.ok(updatedDepartment);
    }

    /**
     * Deletes a department by ID.
     *
     * @param departmentId Department ID
     * @return 204 NO CONTENT status
     */
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID departmentId) {
        log.info("DELETE /api/departments/{} - Deleting department", departmentId);
        departmentService.deleteDepartment(departmentId);
        return ResponseEntity.noContent().build();
    }
}