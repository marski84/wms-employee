package org.localhost.wmsemployee.service;

import org.localhost.wmsemployee.dto.UserLoginDto;

public interface EmployeeCredentialsQueryService {
    boolean validateUserLogin(UserLoginDto userLoginDto);
}
