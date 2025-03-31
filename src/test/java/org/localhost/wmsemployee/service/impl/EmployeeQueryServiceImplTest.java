package org.localhost.wmsemployee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.localhost.wmsemployee.InMemoryRepository.InMemoryEmployeeQueryRepository;
import org.localhost.wmsemployee.InMemoryRepository.TestUtils;
import org.localhost.wmsemployee.dto.EmployeeDataDto;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.service.EmployeeQueryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeQueryServiceImplTest {

    private final int EXPECTED_NUMBER_OF_EMPLOYEES = 2;
    private EmployeeQueryService objectUnderTest;
    private InMemoryEmployeeQueryRepository employeeQueryRepository;
    private Employee testEmployee;
    private Employee secondTestEmployee;

    @BeforeEach
    void setUp() {
        employeeQueryRepository = new InMemoryEmployeeQueryRepository();
        objectUnderTest = new EmployeeQueryServiceImpl(employeeQueryRepository);

        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();

        EmployeeRegistrationDto secondEmployeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        secondEmployeeRegistrationDto.setSupervisorId(2L);
        secondEmployeeRegistrationDto.setEmployeeRole(EmployeeRole.MANAGER);

        testEmployee = employeeQueryRepository.save(Employee.fromRegistrationDto(employeeRegistrationDto));
        secondTestEmployee = employeeQueryRepository.save(Employee.fromRegistrationDto(secondEmployeeRegistrationDto));


    }

    @Test
    @DisplayName("getEmployeeById should successfully return employee data")
    void getEmployeeById() {
//        when
        EmployeeDataDto testResult = objectUnderTest.getEmployeeById(testEmployee.getId());
//        then
        assertAll(
                () -> assertEquals(testEmployee.getId(), testResult.getId()),
                () -> assertEquals(testEmployee.getName(), testResult.getName()),
                () -> assertEquals(testEmployee.getEmployeeRole(), testResult.getEmployeeRole())
        );

    }

    @Test
    @DisplayName("getEmployees should return list of employees")
    void getEmployees() {
//        given, when
        List<EmployeeDataDto> testResult = objectUnderTest.getEmployees();
//        then
        assertAll(
                () -> assertEquals(EXPECTED_NUMBER_OF_EMPLOYEES, testResult.size()),
                () -> assertEquals(testEmployee.getId(), testResult.get(0).getId()),
                () -> assertEquals(secondTestEmployee.getId(), testResult.get(1).getId())
        );
    }

    @Test
    @DisplayName("getEmployeesBySupervisor should return list of employees for supervisor")
    void getEmployeesBySupervisor() {
//        given
        Long firstSupervisorId = testEmployee.getSupervisorId();
        Long secondSupervisorId = secondTestEmployee.getSupervisorId();
//        when
        List<EmployeeDataDto> firstResult = objectUnderTest.getEmployeesBySupervisor(firstSupervisorId);
        List<EmployeeDataDto> secondResult = objectUnderTest.getEmployeesBySupervisor(secondSupervisorId);
//        then
        assertAll(
                () -> assertTrue(firstResult.contains(EmployeeDataDto.fromEmployee(testEmployee))),
                () -> assertFalse(firstResult.contains(EmployeeDataDto.fromEmployee(secondTestEmployee))),
                () -> assertTrue(secondResult.contains(EmployeeDataDto.fromEmployee(secondTestEmployee))),
                () -> assertFalse(secondResult.contains(EmployeeDataDto.fromEmployee(testEmployee)))
        );
    }

    @Test
    @DisplayName("getEmployeesByEmployeeRole should return list of employees")
    void getEmployeesByEmployeeRole() {
//        given
        EmployeeRole firstEmployeeRole = testEmployee.getEmployeeRole();
        secondTestEmployee.setEmployeeRole(EmployeeRole.EMPLOYEE);
        employeeQueryRepository.save(secondTestEmployee);
        EmployeeRole secondEmployeeRole = secondTestEmployee.getEmployeeRole();
//        when
        List<EmployeeDataDto> testResult = objectUnderTest.getEmployeesByEmployeeRole(EmployeeRole.EMPLOYEE);
//        then
        assertAll(
                () -> assertEquals(EXPECTED_NUMBER_OF_EMPLOYEES, testResult.size()),
                () -> assertEquals(firstEmployeeRole, testResult.get(0).getEmployeeRole()),
                () -> assertEquals(secondEmployeeRole, testResult.get(1).getEmployeeRole())
        );
    }

    @Test
    @DisplayName("getEmployeeByStatus should return a list of employees with status")
    void getEmployeeByStatus() {
//        given
        testEmployee.setEmployeeStatus(EmployeeStatus.HOLIDAY);
        secondTestEmployee.setEmployeeStatus(EmployeeStatus.HOLIDAY);
        employeeQueryRepository.save(testEmployee);
        employeeQueryRepository.save(secondTestEmployee);

        EmployeeStatus firstEmployeeStatus = objectUnderTest.getEmployeeById(testEmployee.getId()).getEmployeeStatus();
        EmployeeStatus secondEmployeeStatus = objectUnderTest.getEmployeeById(secondTestEmployee.getId()).getEmployeeStatus();
//        when
        List<EmployeeDataDto> testResult = objectUnderTest.getEmployeeByStatus(EmployeeStatus.HOLIDAY);
//        then
        assertAll(
                () -> assertEquals(EXPECTED_NUMBER_OF_EMPLOYEES, testResult.size()),
                () -> assertEquals(firstEmployeeStatus, testResult.get(0).getEmployeeStatus()),
                () -> assertEquals(secondEmployeeStatus, testResult.get(1).getEmployeeStatus())
        );
    }
}