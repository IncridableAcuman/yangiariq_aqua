package com.aquaai.shared.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.security.rate-limit")
public class RateLimiterProperties {

    /**
     * Whether rate limiting is enabled.
     */
    private boolean enabled = true;

    /**
     * Maximum requests per minute for login attempts per client IP.
     */
    private int loginLimitPerMinute = 10;

    /**
     * Maximum requests per minute for token refresh attempts per client IP.
     */
    private int refreshLimitPerMinute = 30;

    /**
     * Maximum requests per minute for password reset / registration endpoints per client IP.
     */
    private int defaultAuthLimitPerMinute = 15;
}
