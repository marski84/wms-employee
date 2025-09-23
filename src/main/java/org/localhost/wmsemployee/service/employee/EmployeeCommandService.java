package org.localhost.wmsemployee.service.employee;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.registration.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.registration.EmployeeAuthDataDto;
import org.localhost.wmsemployee.dto.registration.EmployeeRegistrationDto;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmployeeCommandService {
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final RestTemplate restTemplate;
    private final EmployeeDataService employeeDataService;

    @Value("${auth0.api.users-endpoint}")
    private String usersEndpoint;

    public EmployeeCommandService(Auth0ManagementTokenService auth0ManagementTokenService, RestTemplate restTemplate, EmployeeDataService employeeDataService) {
        this.auth0ManagementTokenService = auth0ManagementTokenService;
        this.restTemplate = restTemplate;
        this.employeeDataService = employeeDataService;
    }

    /**
     * Registers a new employee in Auth0.
     *
     * @param employeeRegistrationDto The employee registration data
     * @return Auth0RegistrationDto containing the Auth0 response with user details
     */
    @Transactional(rollbackFor = Exception.class)
    public Auth0RegistrationDto registerEmployee(EmployeeRegistrationDto employeeRegistrationDto) {
        Auth0RegistrationDto employeeDto = createAuth0User(employeeRegistrationDto);

        try {
            employeeDataService.save(employeeDto);
        } catch (Exception e) {
            log.error("Failed to save employee to the database. Attempting to delete user from Auth0.", e);
            deleteAuth0User(employeeDto.getUserId());
            throw e; // Re-throw the exception to trigger transactional rollback
        }

        return employeeDto;
    }

    private Auth0RegistrationDto createAuth0User(EmployeeRegistrationDto employeeRegistrationDto) {
        String managementToken = auth0ManagementTokenService.getAccessToken();
        EmployeeAuthDataDto employeeAuthDataDto = EmployeeAuthDataDto.fromEmployee(employeeRegistrationDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + managementToken);
        HttpEntity<EmployeeAuthDataDto> request = new HttpEntity<>(employeeAuthDataDto, headers);

        return restTemplate.postForObject(usersEndpoint, request, Auth0RegistrationDto.class);
    }

    private void deleteAuth0User(String userId) {
        try {
            String managementToken = auth0ManagementTokenService.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + managementToken);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = usersEndpoint + "/" + userId;
            restTemplate.exchange(url, org.springframework.http.HttpMethod.DELETE, request, Void.class);
            log.info("Successfully deleted user {} from Auth0.", userId);
        } catch (Exception e) {
            log.error("Failed to delete user {} from Auth0.", userId, e);
            // We are already in a failure scenario, so we just log this error and continue.
            // The original exception will be re-thrown.
        }
    }
}
