package org.localhost.wmsemployee.service;

import org.localhost.wmsemployee.dto.credentials.ChangePasswordDto;
import org.localhost.wmsemployee.dto.credentials.ResetTokenDto;
import org.localhost.wmsemployee.dto.credentials.UserLoginDto;

public interface EmployeeCredentialsQueryService {
    boolean validateUserLogin(UserLoginDto userLoginDto);

    void setResetToken(ResetTokenDto resetToken);

    void changePassword(ChangePasswordDto changePasswordDto);
}
