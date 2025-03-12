package org.localhost.wmsemployee.mappers;

import org.localhost.wmsemployee.dto.UpdateEmployeeDto;
import org.localhost.wmsemployee.model.Employee;

public class FromUpdateDtoToEmployeeMapper {
    public static Employee mapFromUpdateDtoToEmployee(UpdateEmployeeDto updateEmployeeDto, Employee employee) {
        employee.setName(updateEmployeeDto.getName());
        employee.setSurname(updateEmployeeDto.getSurname());
        employee.getEmployeeContactDetails().setEmail(updateEmployeeDto.getEmail());
        employee.getEmployeeContactDetails().setPhoneNumber(updateEmployeeDto.getPhoneNumber());
        employee.setEmployeeStatus(updateEmployeeDto.getStatus());

        employee.getEmployeeContactDetails().setAddress(updateEmployeeDto.getAddress());
        employee.getEmployeeContactDetails().setCity(updateEmployeeDto.getCity());
        employee.getEmployeeContactDetails().setAddress(updateEmployeeDto.getAddress());
        employee.getEmployeeContactDetails().setCountry(updateEmployeeDto.getCountry());
        employee.getEmployeeContactDetails().setPostalCode(updateEmployeeDto.getPostalCode());

        return employee;
    }
}
