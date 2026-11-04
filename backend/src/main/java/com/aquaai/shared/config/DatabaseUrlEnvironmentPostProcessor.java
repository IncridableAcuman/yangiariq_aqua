package com.aquaai.shared.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalizes cloud PostgreSQL connection strings (e.g., from Neon, Render, Railway, Supabase)
 * into standard Spring Boot JDBC properties (spring.datasource.url, spring.datasource.username,
 * spring.datasource.password).
 *
 * Handles standard PostgreSQL URIs such as:
 *   postgresql://user:password@host:port/database?sslmode=require
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "databaseUrlCustomizer";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> props = new HashMap<>();

        // 1. Resolve raw database URL from environment or .env files
        String rawUrl = findProperty(environment, "DATABASE_URL", "DB_URL", "SPRING_DATASOURCE_URL");

        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = readFromDotEnvFiles("DATABASE_URL", "DB_URL");
        }

        if (rawUrl != null && !rawUrl.isBlank()) {
            parseAndApplyDatabaseUrl(rawUrl, environment, props);
        }

        // 2. Render passes PORT environment variable (e.g. 10000)
        String port = findProperty(environment, "PORT");
        if (port != null && !port.isBlank()) {
            props.put("server.port", port);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        }
    }

    private void parseAndApplyDatabaseUrl(String rawUrl, ConfigurableEnvironment env, Map<String, Object> props) {
        String cleaned = cleanValue(rawUrl);
        String uriString = cleaned;
        if (uriString.startsWith("jdbc:")) {
            uriString = uriString.substring("jdbc:".length());
        }

        if (uriString.startsWith("postgres://") || uriString.startsWith("postgresql://")) {
            try {
                URI uri = URI.create(uriString);
                String userInfo = uri.getUserInfo();
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }
                String query = uri.getQuery();

                if (userInfo != null && !userInfo.isBlank()) {
                    String[] userParts = userInfo.split(":", 2);
                    String explicitUser = findProperty(env, "DATABASE_USERNAME", "SPRING_DATASOURCE_USERNAME", "DB_USER");
                    if (explicitUser == null || explicitUser.isBlank()) {
                        props.put("spring.datasource.username", userParts[0]);
                    }
                    if (userParts.length > 1) {
                        String explicitPass = findProperty(env, "DATABASE_PASSWORD", "SPRING_DATASOURCE_PASSWORD", "DB_PASSWORD");
                        if (explicitPass == null || explicitPass.isBlank()) {
                            props.put("spring.datasource.password", userParts[1]);
                        }
                    }
                }

                if ((query == null || !query.contains("sslmode")) && host != null && host.contains("neon.tech")) {
                    query = (query == null || query.isBlank()) ? "sslmode=require" : query + "&sslmode=require";
                }

                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append("/")
                        .append(path != null ? path : "");

                if (query != null && !query.isBlank()) {
                    jdbcUrl.append("?").append(query);
                }

                props.put("spring.datasource.url", jdbcUrl.toString());
                props.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
            } catch (Exception ignored) {
                if (!cleaned.startsWith("jdbc:")) {
                    props.put("spring.datasource.url", "jdbc:" + cleaned);
                } else {
                    props.put("spring.datasource.url", cleaned);
                }
            }
        } else if (!cleaned.startsWith("jdbc:")) {
            props.put("spring.datasource.url", "jdbc:" + cleaned);
        }
    }

    private String findProperty(ConfigurableEnvironment env, String... keys) {
        for (String key : keys) {
            String val = env.getProperty(key);
            if (val != null && !val.isBlank()) {
                return val;
            }
            val = System.getenv(key);
            if (val != null && !val.isBlank()) {
                return val;
            }
            val = System.getProperty(key);
            if (val != null && !val.isBlank()) {
                return val;
            }
        }
        return null;
    }

    private String readFromDotEnvFiles(String... keys) {
        List<File> candidates = List.of(
                new File(".env"),
                new File("backend/.env"),
                new File("../.env")
        );

        for (File file : candidates) {
            if (file.exists() && file.isFile()) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (!trimmed.startsWith("#") && trimmed.contains("=")) {
                            String[] parts = trimmed.split("=", 2);
                            String key = parts[0].trim();
                            for (String targetKey : keys) {
                                if (key.equalsIgnoreCase(targetKey)) {
                                    return parts[1].trim();
                                }
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    private String cleanValue(String raw) {
        String val = raw.trim();
        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
            return val.substring(1, val.length() - 1).trim();
        }
        return val;
    }
}
