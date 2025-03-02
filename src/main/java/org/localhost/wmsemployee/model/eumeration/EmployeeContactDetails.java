package org.localhost.wmsemployee.model.eumeration;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.localhost.wmsemployee.model.Employee;

import java.time.ZonedDateTime;

@Entity
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

    @NotNull(message = "{employee.not.null.address}")
    @NotBlank(message = "{employee.not.blank.address}")
    private String address;

    ZonedDateTime editDate;



    }

