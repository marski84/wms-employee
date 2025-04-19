package org.localhost.wmsemployee.dto.credentials;

import lombok.Getter;

@Getter
public class ResetTokenDto extends UserLoginDto {
    private final String token;
    private final String newToken;

    public ResetTokenDto(Long userId, String firstName, String lastName, String password, String token, String newToken) {
        super(userId, firstName, lastName, password);
        this.token = token;
        this.newToken = newToken;
    }
}
