package auth;

import auth.service.Auth0ManagementTokenService;
import auth.service.LoginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthConfig {
    @Bean
    public Auth0ManagementTokenService auth0ManagementTokenService(RestTemplate restTemplate) {
        return new Auth0ManagementTokenService(restTemplate);
    }

    @Bean
    public LoginService loginService(RestTemplate restTemplate) {
        return new LoginService(restTemplate);
    }
}
