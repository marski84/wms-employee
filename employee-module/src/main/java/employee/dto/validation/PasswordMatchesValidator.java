package employee.dto.validation;

import employee.dto.CreateUserDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @PasswordMatches annotation.
 * Checks if password and confirmPassword fields are equal.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, CreateUserDto> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(CreateUserDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // Null objects are considered valid (unlikely with @RequestBody, but defensive)
        }

        String password = dto.password();
        String confirmPassword = dto.confirmPassword();

        // Both null is valid (let @NotBlank handle it)
        if (password == null && confirmPassword == null) {
            return true;
        }

        // One null, one not null is invalid
        if (password == null || confirmPassword == null) {
            return false;
        }

        return password.equals(confirmPassword);
    }
}