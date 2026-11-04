package com.aquaai.shared.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    @DisplayName("Should parse standard Neon postgresql URI into clean JDBC URL and credentials")
    void shouldParseNeonPostgresqlUri() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "DATABASE_URL", "postgresql://neondb_owner:npg_secret123@ep-fragrant-mouse-123.neon.tech/neondb?sslmode=require&channel_binding=require"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://ep-fragrant-mouse-123.neon.tech:5432/neondb?sslmode=require&channel_binding=require");
        assertThat(environment.getProperty("spring.datasource.username"))
                .isEqualTo("neondb_owner");
        assertThat(environment.getProperty("spring.datasource.password"))
                .isEqualTo("npg_secret123");
    }

    @Test
    @DisplayName("Should parse postgres scheme and custom port")
    void shouldParsePostgresSchemeWithCustomPort() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "DB_URL", "postgres://user:pass@db.example.com:5433/custom_db"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com:5433/custom_db");
        assertThat(environment.getProperty("spring.datasource.username"))
                .isEqualTo("user");
        assertThat(environment.getProperty("spring.datasource.password"))
                .isEqualTo("pass");
    }

    @Test
    @DisplayName("Should bind Render PORT env var to server.port")
    void shouldBindRenderPort() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "PORT", "10000"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("server.port")).isEqualTo("10000");
    }

    @Test
    @DisplayName("Should keep existing JDBC URL unchanged if already in JDBC format")
    void shouldKeepExistingJdbcUrl() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "DATABASE_URL", "jdbc:postgresql://localhost:5432/yangiariq_aqua"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://localhost:5432/yangiariq_aqua");
    }

    @Test
    @DisplayName("Should skip overriding datasource when test profile is active")
    void shouldSkipWhenTestProfileActive() {
        ConfigurableEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles("test");
        environment.getPropertySources().addFirst(new MapPropertySource("testEnv", Map.of(
                "DATABASE_URL", "postgresql://someuser:somepass@somehost:5432/somedb"
        )));

        processor.postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("spring.datasource.url")).isNull();
    }
}
