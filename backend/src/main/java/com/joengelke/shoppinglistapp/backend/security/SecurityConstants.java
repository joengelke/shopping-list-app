package com.joengelke.shoppinglistapp.backend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("security")
public final class SecurityConstants {
    private String authLoginUrl;

    // Signing key for HS512-Algorithm
    private String jwtSecret;

    // JWT token default values
    private String tokenHeader;
    private String tokenPrefix;
    private String tokenType;
    private String tokenIssuer;
    private String tokenAudience;

    private long expirationTime;
}
