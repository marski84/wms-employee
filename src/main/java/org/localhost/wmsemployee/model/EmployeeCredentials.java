package org.localhost.wmsemployee.model;

import jakarta.persistence.*;
import lombok.*;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.exceptions.PasswordDoesNotMatchConfirmPasswordException;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employee_credentials")
public class EmployeeCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String passwordHash;

    private ZonedDateTime lastPasswordChange;

    private String resetToken;

    private ZonedDateTime resetTokenExpiry;

    private Integer failedAttempt;


    public static EmployeeCredentials fromEmployee(EmployeeRegistrationDto employeeRegistrationDto, Employee employee) {
        if (!employeeRegistrationDto.getPassword().equals(employeeRegistrationDto.getConfirmPassword())) {
            throw new PasswordDoesNotMatchConfirmPasswordException();
        }
        return EmployeeCredentials.builder()
                .employee(employee)
                .passwordHash(employeeRegistrationDto.getPassword())
                .lastPasswordChange(ZonedDateTime.now())
                .failedAttempt(0)
                .resetTokenExpiry(ZonedDateTime.now().plusDays(60))
                .resetToken("DEFAULT_RESET_TOKEN")
                .build();
    }

}
