package org.localhost.wmsemployee.dto.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the token response from authentication service.
 * Contains access token, ID token, token type, and expiration information.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDto {

    private String access_token;
    private String id_token;
    private String token_type;
    private String expires_in;
}