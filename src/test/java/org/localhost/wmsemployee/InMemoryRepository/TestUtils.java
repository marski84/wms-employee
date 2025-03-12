package org.localhost.wmsemployee.InMemoryRepository;

import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;

public final class TestUtils {


    public static EmployeeRegistrationDto getEmployeeRegistrationDto() {
        EmployeeRegistrationDto employeeRegistrationDto = new EmployeeRegistrationDto();
        employeeRegistrationDto.setName("test");
        employeeRegistrationDto.setSurname("test");
        employeeRegistrationDto.setEmployeeRole(EmployeeRole.EMPLOYEE);
        employeeRegistrationDto.setSupervisorId(1L);

        employeeRegistrationDto.setPassword("test");
        employeeRegistrationDto.setConfirmPassword("test");

        employeeRegistrationDto.setPhoneNumber("1234567890");
        employeeRegistrationDto.setEmail("test@test.com");
        employeeRegistrationDto.setAddress("test address");
        employeeRegistrationDto.setCity("test city");
        employeeRegistrationDto.setCountry("test country");
        employeeRegistrationDto.setPostalCode("11111");
        return employeeRegistrationDto;
    }
}
