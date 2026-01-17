package employee;

import employee.repository.EmployeeDataAccess;
import employee.service.UserService;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableJpaRepositories(basePackages = "employee.repository")
@EntityScan(basePackages = "employee.model")
@ComponentScan(basePackages = "employee")
public class EmployeeConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService(
            EmployeeDataAccess dataAccess,
            PasswordEncoder passwordEncoder) {
        return new UserService(dataAccess, passwordEncoder);
    }

}
