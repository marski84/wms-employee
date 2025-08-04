package org.localhost.wmsemployee.service.employee;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.Auth0RegistrationDto;
import org.localhost.wmsemployee.dto.EmployeeAuthDataDto;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
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
        String managementToken = auth0ManagementTokenService.getAccessToken();
        EmployeeAuthDataDto employeeAuthDataDto = EmployeeAuthDataDto.fromEmployee(employeeRegistrationDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + managementToken);
        HttpEntity<EmployeeAuthDataDto> request = new HttpEntity<>(employeeAuthDataDto, headers);

        Auth0RegistrationDto employeeDto = restTemplate.postForObject(usersEndpoint, request, Auth0RegistrationDto.class);
        employeeDataService.save(employeeDto);

        return employeeDto;
    }
}
