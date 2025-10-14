package org.localhost.wmsemployee;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class WmsEmployeeApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename("auth0.env").load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        ConfigurableApplicationContext app = SpringApplication.run(WmsEmployeeApplication.class, args);
    }
}
