package org.localhost.wmsemployee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WmsEmployeeApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(WmsEmployeeApplication.class, args);

//        EmployeeService employeeService = app.getBean(EmployeeService.class);
//        Employee newEmployee = new Employee();
//        employeeService.registerNewEmployee(newEmployee);
    }
}
