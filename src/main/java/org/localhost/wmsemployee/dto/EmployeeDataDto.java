package org.localhost.wmsemployee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.localhost.wmsemployee.model.Employee;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDataDto {
    // Dane podstawowe pracownika
    private Long id;
    private Long supervisorId;
    private String name;
    private String surname;
    private EmployeeRole employeeRole;
    private EmployeeStatus employeeStatus;
    private ZonedDateTime registrationDate;

    // Dane kontaktowe
    private String email;
    private String phoneNumber;
    private String address;
    private String city;
    private String postalCode;
    private String country;
    private ZonedDateTime contactDetailsEditDate;

    // Podstawowe dane uwierzytelniające (bez wrażliwych informacji)
    private ZonedDateTime lastPasswordChange;
    private Integer failedAttempts;

    public static EmployeeDataDto fromEmployee(Employee employee) {
        return EmployeeDataDto.builder()
                .id(employee.getId())
                .supervisorId(employee.getSupervisorId())
                .name(employee.getName())
                .surname(employee.getSurname())
                .employeeRole(employee.getEmployeeRole())
                .employeeStatus(employee.getEmployeeStatus())
                .registrationDate(employee.getRegistrationDate())
                // Dane kontaktowe
                .email(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getEmail() : null)
                .phoneNumber(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getPhoneNumber() : null)
                .address(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getAddress() : null)
                .city(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getCity() : null)
                .postalCode(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getPostalCode() : null)
                .country(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getCountry() : null)
                .contactDetailsEditDate(employee.getEmployeeContactDetails() != null ? employee.getEmployeeContactDetails().getEditDate() : null)
                // Dane uwierzytelniające
                .lastPasswordChange(employee.getCredentials() != null ? employee.getCredentials().getLastPasswordChange() : null)
                .failedAttempts(employee.getCredentials() != null ? employee.getCredentials().getFailedAttempt() : null)
                .build();
    }
}