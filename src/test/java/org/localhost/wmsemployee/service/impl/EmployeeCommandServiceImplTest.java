package org.localhost.wmsemployee.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.localhost.wmsemployee.InMemoryRepository.InMemoryEmployeeRepository;
import org.localhost.wmsemployee.InMemoryRepository.TestUtils;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.exceptions.EmployeeNotFoundException;
import org.localhost.wmsemployee.exceptions.NotAValidSupervisorException;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;
import org.localhost.wmsemployee.repository.EmployeeRepository;
import org.localhost.wmsemployee.service.EmployeeCommandService;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeCommandServiceImplTest {

    private EmployeeCommandService objectUnderTest;
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository = new InMemoryEmployeeRepository();
        objectUnderTest = new EmployeeCommandServiceImpl(employeeRepository);
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
                () -> assertEquals(testResult.getId(), testResult.getEmployeeContactDetails().getEmployee().getId()),
                () -> assertEquals(testResult.getId(), testResult.getCredentials().getEmployee().getId())
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
        Employee testResult = objectUnderTest.updateEmployeeDataBySupervisor(updateEmployeeDto, newEmployeeRole, employee.getSupervisorId());
//        then
        assertAll(
                () -> assertEquals(updateEmployeeDto.getEmployeeId(), testResult.getId()),
                () -> assertEquals(updateEmployeeDto.getCity(), testResult.getEmployeeContactDetails().getCity()),
                () -> assertEquals(updateEmployeeDto.getAddress(), testResult.getEmployeeContactDetails().getAddress()),
                () -> assertEquals(updateEmployeeDto.getCountry(), testResult.getEmployeeContactDetails().getCountry()),
                () -> assertEquals(updateEmployeeDto.getPostalCode(), testResult.getEmployeeContactDetails().getPostalCode()),
                () -> assertEquals(updateEmployeeDto.getEmail(), testResult.getEmployeeContactDetails().getEmail()),
                () -> assertEquals(updateEmployeeDto.getPhoneNumber(), testResult.getEmployeeContactDetails().getPhoneNumber()),
                () -> assertEquals(newEmployeeRole, testResult.getEmployeeRole())
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
        Long NOT_VALID_SUPERVISOR_ID = 20l;
        EmployeeRole newEmployeeRole = EmployeeRole.MANAGER;
//        when
        NotAValidSupervisorException result = assertThrows(
                NotAValidSupervisorException.class,
                () -> objectUnderTest.updateEmployeeDataBySupervisor(updateEmployeeDto, newEmployeeRole, NOT_VALID_SUPERVISOR_ID)
        );
    }
}