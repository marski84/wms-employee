package org.localhost.wmsemployee;

import auth.service.Auth0ManagementTokenService;
import employee.service.UserService;
import org.localhost.wmsemployee.service.employee.Auth0CommandService;
import org.localhost.wmsemployee.service.employee.EmployeeCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Central configuration for wms-app module.
 * Follows the same pattern as AuthConfig and EmployeeConfig:
 * - @ComponentScan for controllers, exception handlers, and other components
 * - Explicit @Bean definitions for services
 */
@Configuration
@ComponentScan(basePackages = "org.localhost.wmsemployee")
public class WmsAppConfig {

    @Bean
    public Auth0CommandService auth0CommandService(
            RestClient restClient,
            Auth0ManagementTokenService auth0ManagementTokenService) {
        return new Auth0CommandService(restClient, auth0ManagementTokenService);
    }

    @Bean
    public EmployeeCommandService employeeCommandService(
            Auth0CommandService auth0CommandService,
            UserService userService) {
        return new EmployeeCommandService(auth0CommandService, userService);
    }
}