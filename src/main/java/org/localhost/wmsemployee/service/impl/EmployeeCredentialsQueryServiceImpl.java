package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.UserLoginDto;
import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.localhost.wmsemployee.repository.impl.EmployeeCredentialsRepository;
import org.localhost.wmsemployee.service.EmployeeCredentialsQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeCredentialsQueryServiceImpl implements EmployeeCredentialsQueryService {
    private final EmployeeCredentialsRepository employeeCredentialsRepository;

    public EmployeeCredentialsQueryServiceImpl(EmployeeCredentialsRepository employeeCredentialsRepository) {
        this.employeeCredentialsRepository = employeeCredentialsRepository;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean validateUserLogin(UserLoginDto userLoginDto) {
        EmployeeCredentials employeeCredentials = employeeCredentialsRepository.findById(userLoginDto.getUserId());
        boolean userAuthorized = userLoginDto.getFirstName().equals(employeeCredentials.getEmployee().getName())
                && userLoginDto.getLastName().equals(employeeCredentials.getEmployee().getSurname())
                && userLoginDto.getPassword().equals(employeeCredentials.getEmployee().getCredentials().getPasswordHash());

        if (!userAuthorized) {
            int failedAttempts = employeeCredentials.getFailedAttempt();
            employeeCredentials.setFailedAttempt(failedAttempts + 1);
            employeeCredentialsRepository.save(employeeCredentials);
        }

        return userAuthorized;
    }
}
