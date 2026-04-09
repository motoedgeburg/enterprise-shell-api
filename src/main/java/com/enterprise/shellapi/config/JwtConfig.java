package com.enterprise.shellapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "okta")
public class JwtConfig {

    private String issuer;
    private String clientId;
}
