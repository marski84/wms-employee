package org.localhost.wmsemployee.service.employee;

import lombok.extern.slf4j.Slf4j;
import org.localhost.wmsemployee.dto.EmployeeRegistrationDto;
import org.localhost.wmsemployee.service.auth.model.EmployeeAuthDataDto;
import org.localhost.wmsemployee.service.auth.service.Auth0ManagementTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class EmployeeCommandService {
    private final Auth0ManagementTokenService auth0ManagementTokenService;
    private final RestTemplate restTemplate;

    @Value("${auth0.m2m.audience}")
    private String url;

    public EmployeeCommandService(Auth0ManagementTokenService auth0ManagementTokenService, RestTemplate restTemplate) {
        this.auth0ManagementTokenService = auth0ManagementTokenService;
        this.restTemplate = restTemplate;
    }

    public EmployeeAuthDataDto registerEmployee(EmployeeRegistrationDto employeeRegistrationDto) {
        String managmentToken = auth0ManagementTokenService.getAccessToken();
        EmployeeAuthDataDto employeeAuthDataDto = EmployeeAuthDataDto.fromEmployee(employeeRegistrationDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + managmentToken);
        HttpEntity<EmployeeAuthDataDto> request = new HttpEntity<>(employeeAuthDataDto, headers);

        return restTemplate.postForObject(url + "/users", request, EmployeeAuthDataDto.class);
    }
}
