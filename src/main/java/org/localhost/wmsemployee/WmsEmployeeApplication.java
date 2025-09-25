package org.localhost.wmsemployee;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WmsEmployeeApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().filename("auth0.env").load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        ConfigurableApplicationContext app = SpringApplication.run(WmsEmployeeApplication.class, args);

//        EmployeeService employeeService = app.getBean(EmployeeService.class);
//        Employee newEmployee = new Employee();
//        employeeService.registerNewEmployee(newEmployee);
    }
}
