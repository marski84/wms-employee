package org.localhost.wmsemployee;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Slf4j
@Import({
        org.localhost.wmsemployee.config.AppConfig.class,
        employee.EmployeeConfig.class,
        auth.AuthConfig.class
})
public class WmsEmployeeApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename("auth0.env").load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(WmsEmployeeApplication.class, args);
    }
}
