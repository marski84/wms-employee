package org.localhost.wmsemployee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.localhost.wmsemployee.model.eumeration.EmployeeRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmployeeRegistrationDto {

    @NotNull(message = "Supervisor ID is required")
    private Long supervisorId;

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

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long and include at least one digit, one lowercase letter, one uppercase letter, and one special character")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotNull(message = "Employee role is required")
    private EmployeeRole employeeRole;


    // Adres - pola opcjonalne
    private String address;
    private String city;
    private String postalCode;
    private String country;
}