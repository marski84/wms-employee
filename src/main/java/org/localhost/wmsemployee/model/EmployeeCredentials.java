package org.localhost.wmsemployee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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
    private LocalDateTime lastPasswordChange;

    @NotNull
    private String resetToken;

    @NotNull
    private LocalDateTime resetTokenExpiry;

    @NotNull
    private Integer failedAttempts;


}
