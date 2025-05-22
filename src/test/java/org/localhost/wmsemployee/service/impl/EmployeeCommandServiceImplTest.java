package org.localhost.wmsemployee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.localhost.wmsemployee.InMemoryRepository.InMemoryEmployeeCommandRepository;
import org.localhost.wmsemployee.InMemoryRepository.TestUtils;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.exceptions.NotAValidSupervisorException;
import org.localhost.wmsemployee.exceptions.NotAllowedStatusTransition;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeCommand.EmployeeCommandRepository;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.localhost.wmsemployee.service.employee.impl.EmployeeCommandServiceImpl;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeCommandServiceImplTest {

    private EmployeeCommandService objectUnderTest;
    private EmployeeCommandRepository employeeCommandRepository;

    @BeforeEach
    void setUp() {
        employeeCommandRepository = new InMemoryEmployeeCommandRepository();
        objectUnderTest = new EmployeeCommandServiceImpl(employeeCommandRepository);
    }

    @Test
    @DisplayName("registerEmployee should register a new employee")
    void registerNewEmployee() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();

//        when
        Employee testResult = objectUnderTest.registerNewEmployee(employeeRegistrationDto);
//        then
        assertAll(
                () -> assertEquals(employeeRegistrationDto.getName(), testResult.getName()),
                () -> assertEquals(employeeRegistrationDto.getSurname(), testResult.getSurname()),
                () -> assertEquals(employeeRegistrationDto.getEmployeeRole(), testResult.getEmployeeRole()),
                () -> assertEquals(testResult.getId(), testResult.getEmployeeContactDetails().getEmployee().getId())
        );
    }

    @Test
    @DisplayName("updateEmployee should update employeeData")
    void updateEmployee() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);

        UpdateEmployeeDto updateEmployeeDto = UpdateEmployeeDto.builder()
                .employeeId(employee.getId())
                .email("updated@o2.pl")
                .city("updated city")
                .surname("updated surname")
                .name("updated name")
                .address("updated address")
                .phoneNumber("updated phone number")
                .country("updated country")
                .postalCode("updated postal code")
                .status(EmployeeStatus.HOLIDAY)
                .build();
//        when
        Employee testResult = objectUnderTest.updateEmployee(updateEmployeeDto);
//        then
        assertAll(
                () -> assertEquals(updateEmployeeDto.getEmployeeId(), testResult.getId()),
                () -> assertEquals(updateEmployeeDto.getCity(), testResult.getEmployeeContactDetails().getCity()),
                () -> assertEquals(updateEmployeeDto.getAddress(), testResult.getEmployeeContactDetails().getAddress()),
                () -> assertEquals(updateEmployeeDto.getCountry(), testResult.getEmployeeContactDetails().getCountry()),
                () -> assertEquals(updateEmployeeDto.getPostalCode(), testResult.getEmployeeContactDetails().getPostalCode()),
                () -> assertEquals(updateEmployeeDto.getEmail(), testResult.getEmployeeContactDetails().getEmail()),
                () -> assertEquals(updateEmployeeDto.getPhoneNumber(), testResult.getEmployeeContactDetails().getPhoneNumber())
        );
    }

    @Test
    @DisplayName("updateEmployee should throw when employee not found")
    void updateEmployeeNotFound() {
//        given
        UpdateEmployeeDto updateEmployeeDto = UpdateEmployeeDto.builder()
                .employeeId(1L)
                .build();
//        when & then
        EmployeeNotFoundException result = assertThrows(
                EmployeeNotFoundException.class,
                () -> objectUnderTest.updateEmployee(updateEmployeeDto)
        );
    }

    @Test
    @DisplayName("updateEmployeeBySupervisor should successfully update employee role")
    void updateEmployeeDataBySupervisor() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);

        UpdateEmployeeDto updateEmployeeDto = UpdateEmployeeDto.builder()
                .employeeId(employee.getId())
                .email("updated@o2.pl")
                .city("updated city")
                .surname("updated surname")
                .name("updated name")
                .address("updated address")
                .phoneNumber("updated phone number")
                .country("updated country")
                .postalCode("updated postal code")
                .status(EmployeeStatus.HOLIDAY)
                .build();

        EmployeeRole newEmployeeRole = EmployeeRole.MANAGER;
//        when
        Employee testResult = objectUnderTest.updateEmployeeDataBySupervisor(updateEmployeeDto, employee.getSupervisorId());
//        then
        assertAll(
                () -> assertEquals(updateEmployeeDto.getEmployeeId(), testResult.getId()),
                () -> assertEquals(updateEmployeeDto.getCity(), testResult.getEmployeeContactDetails().getCity()),
                () -> assertEquals(updateEmployeeDto.getAddress(), testResult.getEmployeeContactDetails().getAddress()),
                () -> assertEquals(updateEmployeeDto.getCountry(), testResult.getEmployeeContactDetails().getCountry()),
                () -> assertEquals(updateEmployeeDto.getPostalCode(), testResult.getEmployeeContactDetails().getPostalCode()),
                () -> assertEquals(updateEmployeeDto.getEmail(), testResult.getEmployeeContactDetails().getEmail()),
                () -> assertEquals(updateEmployeeDto.getPhoneNumber(), testResult.getEmployeeContactDetails().getPhoneNumber())
        );
    }

    @Test
    @DisplayName("updateEmployeeBySupervisor should throw when not an employee supervisor tries to update data")
    void updateEmployeeBySupervisorNotAnEmployeeData() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);

        UpdateEmployeeDto updateEmployeeDto = UpdateEmployeeDto.builder()
                .employeeId(employee.getId())
                .email("updated@o2.pl")
                .city("updated city")
                .surname("updated surname")
                .name("updated name")
                .address("updated address")
                .phoneNumber("updated phone number")
                .country("updated country")
                .postalCode("updated postal code")
                .status(EmployeeStatus.HOLIDAY)
                .build();
        Long NOT_VALID_SUPERVISOR_ID = 20L;//        when
        NotAValidSupervisorException result = assertThrows(
                NotAValidSupervisorException.class,
                () -> objectUnderTest.updateEmployeeDataBySupervisor(updateEmployeeDto, NOT_VALID_SUPERVISOR_ID)
        );
    }

    @Test
    @DisplayName("updateEmployeeStatusBySupervisor should successfully update employee status")
    void updateEmployeeStatusBySupervisor() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);
        Long supervisorId = employee.getSupervisorId();
        EmployeeStatus newStatus = EmployeeStatus.HOLIDAY;
//        when
        Employee testResult = objectUnderTest.updateEmployeeStatusBySupervisor(employee.getId(), newStatus, supervisorId);
//        then
        assertAll(
                () -> assertEquals(employee.getId(), testResult.getId()),
                () -> assertEquals(newStatus, testResult.getEmployeeStatus())
        );
    }

    @Test
    @DisplayName("updateEmployeeRoleBySupervisor should successfully update employee role")
    void updateEmployeeRoleBySupervisor() {
        //        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);
        Long supervisorId = employee.getSupervisorId();
        EmployeeRole newRole = EmployeeRole.MANAGER;
//        when
        Employee testResult = objectUnderTest.updateEmployeeRoleBySupervisor(employee.getId(), newRole, supervisorId);
//        then
        assertAll(
                () -> assertEquals(employee.getId(), testResult.getId()),
                () -> assertEquals(newRole, testResult.getEmployeeRole())
        );
    }

    @Test
    @DisplayName("update employeeStatusInternal should successfully update employee status")
    void updateEmployeeStatusInternal() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);
        EmployeeStatus newEmployeeStatus = EmployeeStatus.HOLIDAY;
//        when
        Employee testResult = objectUnderTest.updateEmployeeStatusByEmployee(employee.getSupervisorId(), newEmployeeStatus);
//        then
        assertAll(
                () -> assertEquals(employee.getId(), testResult.getId()),
                () -> assertEquals(employee.getEmployeeStatus(), newEmployeeStatus)
        );
    }

    @Test
    @DisplayName("update employeeStatusInternal should throw when not allowed status")
    void updateEmployeeStatusInternalShouldThrow() {
//        given
        EmployeeRegistrationDto employeeRegistrationDto = TestUtils.getEmployeeRegistrationDto();
        Employee employee = objectUnderTest.registerNewEmployee(employeeRegistrationDto);
        EmployeeStatus newEmployeeStatus = EmployeeStatus.MEDICAL_LEAVE;
        //        when
        NotAllowedStatusTransition testResult =
                assertThrows(
                        NotAllowedStatusTransition.class,
                        () -> objectUnderTest.updateEmployeeStatusByEmployee(employee.getSupervisorId(), newEmployeeStatus)
                );
    }
}
