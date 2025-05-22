package org.localhost.wmsemployee.controller;

import jakarta.validation.constraints.Min;
import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.service.employee.EmployeeQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@Validated
public class EmployeeQueryController {

    private final EmployeeQueryService employeeQueryService;

    public EmployeeQueryController(EmployeeQueryService employeeQueryService) {
        this.employeeQueryService = employeeQueryService;
    }

    @GetMapping
    ResponseEntity<List<EmployeeDataDto>> getEmployees() {
        List<EmployeeDataDto> employees = employeeQueryService.getEmployees();
        if (employees.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    ResponseEntity<EmployeeDataDto> getEmployeeById(
            @PathVariable @Min(value = 1, message = "employee id must be equal or greater than 1") Long id) {
        return ResponseEntity.ok(employeeQueryService.getEmployeeById(id));
    }

    @GetMapping("/supervisor/{supervisorId}")
    ResponseEntity<List<EmployeeDataDto>> getEmployeesBySupervisor(
            @PathVariable
            @Min(value = 1, message = "supervisor id must be equal or greater than 1")
            Long supervisorId) {
        List<EmployeeDataDto> employees = employeeQueryService.getEmployeesBySupervisor(supervisorId);
        if (employees.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/role/{role}")
    ResponseEntity<List<EmployeeDataDto>> getEmployeesByEmployeeRole(@PathVariable EmployeeRole role) {
        List<EmployeeDataDto> employees = employeeQueryService.getEmployeesByEmployeeRole(role);
        if (employees.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/status/{status}")
    ResponseEntity<List<EmployeeDataDto>> getEmployeeByStatus(
            @PathVariable EmployeeStatus status) {
        List<EmployeeDataDto> employees = employeeQueryService.getEmployeeByStatus(status);
        if (employees.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(employees);
    }

}
