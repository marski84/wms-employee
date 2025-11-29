package employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for employee module.
 * Provides password encoder bean for hashing user passwords.
 */
@Configuration
public class SecurityConfig {

    /**
     * BCrypt password encoder with default strength (10 rounds).
     * BCrypt is a secure hashing algorithm designed for passwords.
     *
     * @return PasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}