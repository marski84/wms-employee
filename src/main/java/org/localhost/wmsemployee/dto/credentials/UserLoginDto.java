package org.localhost.wmsemployee.dto.credentials;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginDto {
    private final Long userId;
    private final String firstName;
    private final String lastName;
    private final String password;
}
