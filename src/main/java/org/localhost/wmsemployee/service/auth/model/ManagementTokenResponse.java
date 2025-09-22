package org.localhost.wmsemployee.service.auth.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ManagementTokenResponse {

    private String access_token;
    private String scope;
    private String token_type;
    private int expires_in;


}
