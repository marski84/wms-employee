package org.localhost.wmsemployee.dto.credentials;

import lombok.Getter;

@Getter
public class ChangePasswordDto extends UserLoginDto {
    private final String newPassword;

    public ChangePasswordDto(Long userId, String firstName, String lastName, String password, String newPassword) {
        super(userId, firstName, lastName, password);
        this.newPassword = newPassword;
    }
}
