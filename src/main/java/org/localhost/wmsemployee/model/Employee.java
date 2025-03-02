package org.localhost.wmsemployee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.localhost.wmsemployee.model.eumeration.EmployeeContactDetails;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

import java.time.ZonedDateTime;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "{employee.not.null.supervisorId}")
    @NotBlank(message = "{employee.not.blank.supervisorId}")
    private Long supervisorId;

    @NotNull(message = "{employee.not.null.name}")
    @NotBlank(message = "{employee.not.blank.name}")
    private String name;

    @NotNull(message = "{employee.not.null.surname}")
    @NotBlank(message = "{employee.not.blank.surname}")
    private String surname;

    @NotNull(message = "{employee.not.null.role}")
    private EmployeeRole employeeRole;

    @NotNull(message = "{employee.not.null.status}")
    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;

    private ZonedDateTime registrationDate;


    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private EmployeeCredentials credentials;

    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
    private EmployeeContactDetails employeeContactDetails;


}
