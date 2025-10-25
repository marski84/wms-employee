package org.localhost.wmsemployee.service.employee;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.registration.EmployeeRegistrationDto;
import org.localhost.wmsemployee.service.auth.model.EmployeeData;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class EmployeeCommandService {
    private final RestTemplate restTemplate;
    private final EmployeeDataService employeeDataService;
    private final Auth0ManagementTokenService auth0ManagementTokenService;

    @Value("${auth0.domain}")
    private String auth0Domain;

    @Value("${auth0.api.users-endpoint}")
    private String auth0UsersEndpoint;

    @Value("${auth0.connection:Username-Password-Authentication}")
    private String auth0Connection;


    public EmployeeCommandService(RestTemplate restTemplate,
                                  EmployeeDataService employeeDataService,
                                  Auth0ManagementTokenService auth0ManagementTokenService) {
        this.restTemplate = restTemplate;
        this.employeeDataService = employeeDataService;
        this.auth0ManagementTokenService = auth0ManagementTokenService;
    }

    /**
     * Registers a new employee in Auth0 using the public signup endpoint.
     *
     * @param employeeRegistrationDto The employee registration data
     * @return Auth0RegistrationDto containing the Auth0 response with user details
     */
    @Transactional(rollbackFor = Exception.class)
    public Auth0RegistrationDto registerEmployee(EmployeeRegistrationDto employeeRegistrationDto) {
        Auth0RegistrationDto employeeDto = createAuth0User(employeeRegistrationDto);

        try {
            EmployeeData registeredEmployee = employeeDataService.save(employeeDto);
            log.info("Employee successfully registered in Auth0 and saved to database. User ID: {}", employeeDto.getUserId());
        } catch (Exception e) {
            log.error("Failed to save employee to the database. Attempting to delete user from Auth0.", e);
            throw e; // Re-throw the exception to trigger transactional rollback
        }

        return employeeDto;
    }

    /**
     * Creates a new user in Auth0 using the Management API /api/v2/users endpoint.
     * Requires valid M2M authentication token with create:users scope.
     * Token is automatically managed and refreshed by Auth0ManagementTokenService.
     *
     * @param employeeRegistrationDto The employee registration data
     * @return Auth0RegistrationDto containing the created user details
     */
    private Auth0RegistrationDto createAuth0User(EmployeeRegistrationDto employeeRegistrationDto) {
        // Get a valid M2M token (cached or refreshed if expired)
        String managementToken = auth0ManagementTokenService.getAccessToken();

        // Build user creation payload for Management API
        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("email", employeeRegistrationDto.getEmail());
        userPayload.put("password", employeeRegistrationDto.getPassword());
        userPayload.put("connection", auth0Connection); // Required: database connection name
        userPayload.put("email_verified", false); // User email is not pre-verified

        // Generate username from email (extract part before @)
        String username = employeeRegistrationDto.getEmail().split("@")[0];
        userPayload.put("username", username);

        // Generate nickname from name and surname
        String nickname = employeeRegistrationDto.getName() + " " + employeeRegistrationDto.getSurname();
        userPayload.put("nickname", nickname);

        // Store additional employee data in user_metadata
        Map<String, Object> userMetadata = new LinkedHashMap<>();
        userMetadata.put("name", employeeRegistrationDto.getName());
        userMetadata.put("surname", employeeRegistrationDto.getSurname());
        userMetadata.put("phoneNumber", employeeRegistrationDto.getPhoneNumber());
        userMetadata.put("address", employeeRegistrationDto.getAddress());
        userMetadata.put("city", employeeRegistrationDto.getCity());
        userMetadata.put("postalCode", employeeRegistrationDto.getPostalCode());
        userMetadata.put("country", employeeRegistrationDto.getCountry());
        userMetadata.put("employeeRole", employeeRegistrationDto.getEmployeeRole().name());
        userMetadata.put("employeeStatus", employeeRegistrationDto.getEmployeeStatus());
        userPayload.put("user_metadata", userMetadata);

        // Prepare request headers with Management API authentication
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(managementToken); // Add M2M token in Authorization header
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);

        // Call Auth0 Management API user creation endpoint
        log.info("Creating user in Auth0 using Management API: {} with connection: {}",
                auth0UsersEndpoint, auth0Connection);

        return restTemplate.postForObject(auth0UsersEndpoint, request, Auth0RegistrationDto.class);
    }
}
