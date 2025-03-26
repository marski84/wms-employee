package org.localhost.wmsemployee.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.localhost.wmsemployee.model.eumeration.EmployeeStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeDto {

    @NotNull
    @NotNull
    private Long employeeId;

    @NotBlank(message = "First name is required")
    private String name;

    @NotBlank(message = "Last name is required")
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[(]?[0-9]{3}[)]?[-\\s.]?[0-9]{3}[-\\s.]?[0-9]{4,6}$",
            message = "Please provide a valid phone number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    // Adres - pola opcjonalne
    private String address;
    private String city;
    private String postalCode;
    private String country;
}
