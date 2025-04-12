package org.localhost.wmsemployee.service.impl;

import org.localhost.wmsemployee.dto.credentials.ChangePasswordDto;
import org.localhost.wmsemployee.dto.credentials.ResetTokenDto;
import org.localhost.wmsemployee.dto.credentials.UserLoginDto;
import org.localhost.wmsemployee.exceptions.NotValidTokenException;
import org.localhost.wmsemployee.exceptions.PasswordNotValidException;
import org.localhost.wmsemployee.model.EmployeeCredentials;
import org.localhost.wmsemployee.repository.crud.EmployeeCredentialsRepository;
import org.localhost.wmsemployee.service.EmployeeCredentialsQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeCredentialsServiceImpl implements EmployeeCredentialsQueryService {
    private final EmployeeCredentialsRepository employeeCredentialsRepository;

    public EmployeeCredentialsServiceImpl(EmployeeCredentialsRepository employeeCredentialsRepository) {
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setResetToken(ResetTokenDto resetToken) {
        EmployeeCredentials employeeCredentials = employeeCredentialsRepository.findById(resetToken.getUserId());
        if (!employeeCredentials.getResetToken().equals(resetToken.getToken())) {
            throw new NotValidTokenException();
        }
        employeeCredentials.setResetToken(resetToken.getNewToken());
        employeeCredentialsRepository.save(employeeCredentials);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(ChangePasswordDto changePasswordDto) {
        EmployeeCredentials employeeCredentials = employeeCredentialsRepository.findById(changePasswordDto.getUserId());
        if (!employeeCredentials.getPasswordHash().equals(changePasswordDto.getPassword())) {
            throw new PasswordNotValidException();
        }
        employeeCredentials.setPasswordHash(changePasswordDto.getNewPassword());
        employeeCredentialsRepository.save(employeeCredentials);
    }
}
