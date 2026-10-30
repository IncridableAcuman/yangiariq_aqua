package com.aquaai.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * Secret key for HMAC signing (min 256 bits for HS256 / 512 bits for HS512).
     */
    private String secret;

    /**
     * Access token validity in milliseconds (default: 24 hours).
     */
    private long accessTokenExpirationMs = 86_400_000L;

    /**
     * Refresh token validity in milliseconds (default: 7 days).
     */
    private long refreshTokenExpirationMs = 604_800_000L;
}
