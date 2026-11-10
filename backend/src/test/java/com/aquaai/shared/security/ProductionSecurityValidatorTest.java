package com.aquaai.shared.security;

import com.aquaai.shared.config.CorsProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionSecurityValidatorTest {

    private static final String STRONG_PROD_SECRET = "StrongProdSecretWithSufficientEntropy1234567890!@#$SecureProductionKeyLengthRequirement";

    @Test
    @DisplayName("Should skip validation when active profile is dev or test")
    void shouldSkipWhenNonProd() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(ProductionSecurityValidator.INSECURE_DEV_JWT_SECRET);

        CorsProperties corsProps = new CorsProperties();

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatCode(validator::afterSingletonsInstantiated).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should fail fast in prod when JWT secret is default dev secret")
    void shouldFailWhenProdUsesDevJwtSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "strong_prod_db_pass_9876");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(ProductionSecurityValidator.INSECURE_DEV_JWT_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Default development JWT secret cannot be used in production");
    }

    @Test
    @DisplayName("Should fail fast in prod when JWT secret is too short")
    void shouldFailWhenProdUsesShortJwtSecret() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "strong_prod_db_pass_9876");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret("short-secret-key");

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT secret must be at least 256 bits");
    }

    @Test
    @DisplayName("Should fail fast in prod when DB password is dev default")
    void shouldFailWhenProdUsesDevDbPassword() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", ProductionSecurityValidator.INSECURE_DEV_DB_PASSWORD);

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(STRONG_PROD_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Default development database password cannot be used in production");
    }

    @Test
    @DisplayName("Should fail fast in prod when CORS allowed origins contain localhost")
    void shouldFailWhenProdCorsContainsLocalhost() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "strong_prod_db_pass_9876");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(STRONG_PROD_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("http://localhost:3000", "https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CORS allowed origins must not contain localhost");
    }

    @Test
    @DisplayName("Should fail fast in prod when rate limiting is explicitly disabled")
    void shouldFailWhenProdRateLimitingDisabled() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "prod_db_vault_password_xyz987");
        env.setProperty("app.security.rate-limit.enabled", "false");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(STRONG_PROD_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Rate limiting (app.security.rate-limit.enabled) cannot be explicitly disabled in production");
    }

    @Test
    @DisplayName("Should fail fast in prod when Super Admin bootstrap has default/weak password")
    void shouldFailWhenProdBootstrapHasWeakPassword() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "prod_db_vault_password_xyz987");
        env.setProperty("app.security.bootstrap.super-admin.enabled", "true");
        env.setProperty("app.security.bootstrap.super-admin.password", "Admin123!");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(STRONG_PROD_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatThrownBy(validator::afterSingletonsInstantiated)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Super Admin bootstrap password does not meet security standards");
    }

    @Test
    @DisplayName("Should succeed in prod when all production security requirements are met")
    void shouldSucceedWhenAllProdRequirementsMet() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("spring.datasource.password", "prod_db_vault_password_xyz987");

        JwtProperties jwtProps = new JwtProperties();
        jwtProps.setSecret(STRONG_PROD_SECRET);

        CorsProperties corsProps = new CorsProperties();
        corsProps.setAllowedOrigins(List.of("https://aqua.yangiariq.uz", "https://app.aqua.yangiariq.uz"));

        ProductionSecurityValidator validator = new ProductionSecurityValidator(env, jwtProps, corsProps);

        assertThatCode(validator::afterSingletonsInstantiated).doesNotThrowAnyException();
    }
}
