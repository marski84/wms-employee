package org.localhost.wmsemployee.service.auth.model;


//{
//        "client_id":"eb8k4vqblV3VcluT1kSPA3SvoJpy2cXj",
//        "client_secret":"-04Dbt5PcbAI2Oy3OGwfJzKVu0G2-EumejRHJ5tgYUPZcFRi76Y5eNwRFyRONQ3w",
//        "audience":"https://dev-zh4tehvzvjhkqcud.eu.auth0.com/api/v2/",
//        "grant_type":"client_credentials"
//        }

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ManagmentTokenRequestBody {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("audience")
    private String audience;

}
