package org.localhost.wmsemployee.config;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.config.validator.AudienceValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @Value("${auth0.m2m.audience}")
    private String audience;

    @Value("${auth0.m2m.clientId}")
    private String clientId;

    @Bean
    public CorsConfigurationSource corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:3000", "http://localhost:8080"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**") // Disable CSRF for API endpoints
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/images/**", "/login", "/error").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/token").permitAll() // API login endpoints
                        .requestMatchers("/api/private/**").authenticated()
                        .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(customJwtAuthenticationConverter()
                                )
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .addLogoutHandler(logoutHandler()));
        return http.build();
    }

    private LogoutHandler logoutHandler() {
        return (request, response, authentication) -> {
            try {
                String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
                response.sendRedirect(issuer + "v2/logout?client_id=" + clientId + "&returnTo=" + baseUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        log.info("JWT Decoder: {}", jwtDecoder);
        jwtDecoder.setJwtValidator(withAudience);

        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter customJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("permissions");
        converter.setAuthorityPrefix("SCOPE_");

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);

        return jwtConverter;
    }


}
