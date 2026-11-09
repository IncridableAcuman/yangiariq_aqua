package com.aquaai.shared.security;

import com.aquaai.shared.config.CorsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Validates critical security invariants during application startup.
 * Enforces fail-fast behavior if insecure default development configurations are detected in production.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductionSecurityValidator implements SmartInitializingSingleton {

    public static final String INSECURE_DEV_JWT_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970337336763979244226452948404D635166546A576E5A7234753778214125442A";
    public static final String INSECURE_DEV_DB_PASSWORD = "aqua_secure_password_dev";

    private final Environment environment;
    private final JwtProperties jwtProperties;
    private final CorsProperties corsProperties;

    @Override
    public void afterSingletonsInstantiated() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        if (activeProfiles.contains("prod") || activeProfiles.contains("production")) {
            log.info("Validating production security invariants...");
            validateProductionJwtSecret();
            validateProductionDatabasePassword();
            validateProductionCorsConfiguration();
            log.info("Production security invariants successfully verified.");
        }
    }

    private void validateProductionJwtSecret() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("FATAL: Production startup aborted: JWT secret (JWT_SECRET) is missing or empty.");
        }

        if (INSECURE_DEV_JWT_SECRET.equals(secret)) {
            throw new IllegalStateException("FATAL: Production startup aborted: Default development JWT secret cannot be used in production.");
        }

        if (secret.length() < 32) {
            throw new IllegalStateException("FATAL: Production startup aborted: JWT secret must be at least 256 bits (32 characters).");
        }
    }

    private void validateProductionDatabasePassword() {
        String dbPassword = environment.getProperty("spring.datasource.password");
        if (dbPassword == null) {
            dbPassword = environment.getProperty("DATABASE_PASSWORD");
        }

        if (!StringUtils.hasText(dbPassword)) {
            throw new IllegalStateException("FATAL: Production startup aborted: Database password is missing or empty.");
        }

        if (INSECURE_DEV_DB_PASSWORD.equals(dbPassword)) {
            throw new IllegalStateException("FATAL: Production startup aborted: Default development database password cannot be used in production.");
        }
    }

    private void validateProductionCorsConfiguration() {
        List<String> origins = corsProperties.getAllowedOrigins();
        if (origins != null) {
            for (String origin : origins) {
                if (origin.contains("localhost") || origin.contains("127.0.0.1")) {
                    throw new IllegalStateException("FATAL: Production startup aborted: CORS allowed origins must not contain localhost or loopback in production.");
                }
                if ("*".equals(origin.trim()) && corsProperties.isAllowCredentials()) {
                    throw new IllegalStateException("FATAL: Production startup aborted: Insecure CORS wildcard '*' with credentials is not permitted in production.");
                }
            }
        }
    }
}
