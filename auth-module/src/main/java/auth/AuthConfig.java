package auth;

import auth.service.Auth0ManagementTokenService;
import auth.service.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ComponentScan(basePackages = "auth")
public class AuthConfig {
    @Bean
    public Auth0ManagementTokenService auth0ManagementTokenService(RestClient restClient) {
        return new Auth0ManagementTokenService(restClient);
    }

    @Bean
    public LoginService loginService(RestClient restClient) {
        return new LoginService(restClient);
    }
}
