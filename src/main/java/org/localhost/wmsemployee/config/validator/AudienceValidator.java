package org.localhost.wmsemployee.config.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    @Value("${auth0.m2m.audience}")
    private String audience;


    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String tokenAudience = token.getAudience().contains(audience)
                ? audience
                : null;

        if (tokenAudience == null) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_TOKEN,
                    "The required audience " + audience + " is missing",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
