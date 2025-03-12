package org.localhost.wmsemployee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.localhost.wmsemployee.constants.Constants;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.exceptions.PasswordDoesNotMatchConfirmPasswordException;

import java.time.LocalDateTime;
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

    @NotNull
    private String passwordHash;

    @NotNull
    private ZonedDateTime lastPasswordChange = ZonedDateTime.now(); ;

    @NotNull
    private String resetToken = Constants.INITIAL_RESET_TOKEN;

    @NotNull
    private ZonedDateTime resetTokenExpiry = ZonedDateTime.now().plusDays(60);

    @NotNull
    private Integer failedAttempt = 0;

    public static EmployeeCredentials fromEmployee(EmployeeRegistrationDto employeeRegistrationDto, Employee employee) {
        if (!employeeRegistrationDto.getPassword().equals(employeeRegistrationDto.getConfirmPassword())) {
            throw new PasswordDoesNotMatchConfirmPasswordException();
        }
        return EmployeeCredentials.builder()
                .employee(employee)
                .passwordHash(employeeRegistrationDto.getPassword())
                .build();
    }

}
