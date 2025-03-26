package org.localhost.wmsemployee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee_contact_details")
public class EmployeeContactDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Email(message = "{employee.invalid.email}")
    @NotNull(message = "{employee.not.null.email}")
    @NotBlank(message = "{employee.not.blank.email}")
    private String email;

    @NotNull(message = "{employee.not.null.phoneNumber}")
    @NotBlank(message = "{employee.not.blank.phoneNumber}")
    private String phoneNumber;

    private String address;

    private String city;

    private String postalCode;

    private String country;

    private ZonedDateTime editDate;

    public static EmployeeContactDetails fromEmployeeContactDetails(EmployeeRegistrationDto employeeRegistrationDto, Employee employee) {
        return EmployeeContactDetails.builder()
                .employee(employee)
                .address(employeeRegistrationDto.getAddress())
                .city(employeeRegistrationDto.getCity())
                .postalCode(employeeRegistrationDto.getPostalCode())
                .country(employeeRegistrationDto.getCountry())
                .email(employeeRegistrationDto.getEmail())
                .phoneNumber(employeeRegistrationDto.getPhoneNumber())
                .build();
    }
}