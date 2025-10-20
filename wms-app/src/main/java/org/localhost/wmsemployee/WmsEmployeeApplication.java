package org.localhost.wmsemployee;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
//@ComponentScan(basePackages = {"org.localhost.wmsemployee", "auth"})
public class WmsEmployeeApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename("auth0.env").load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(WmsEmployeeApplication.class, args);
    }
}
