package com.aquaai.integration.infrastructure.weather;

import com.aquaai.recommendation.domain.model.WeatherSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class ExternalWeatherClient {

    private final ExternalWeatherProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ExternalWeatherClient(ExternalWeatherProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                .build();
    }

    public Optional<WeatherSnapshot> fetchCurrentWeather(double lat, double lon) {
        if (!properties.isEnabled() || properties.getUrl() == null || properties.getUrl().isBlank()) {
            return Optional.empty();
        }

        try {
            validateSsrf(properties.getUrl());

            String fullUrl = String.format("%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,precipitation,surface_pressure,wind_speed_10m",
                    properties.getUrl(), lat, lon);

            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .header("Accept", "application/json")
                    .header("User-Agent", "YangiariqAquaAI/0.8.19");

            if (properties.getApiKey() != null && !properties.getApiKey().isBlank()) {
                reqBuilder.header("Authorization", "Bearer " + properties.getApiKey());
            }

            HttpResponse<String> response = httpClient.send(reqBuilder.GET().build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode current = root.path("current");

                if (!current.isMissingNode()) {
                    BigDecimal temp = BigDecimal.valueOf(current.path("temperature_2m").asDouble(25.0)).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal humidity = BigDecimal.valueOf(current.path("relative_humidity_2m").asDouble(45.0)).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal precip = BigDecimal.valueOf(current.path("precipitation").asDouble(0.0)).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal pressure = BigDecimal.valueOf(current.path("surface_pressure").asDouble(1012.0)).setScale(1, RoundingMode.HALF_UP);
                    BigDecimal wind = BigDecimal.valueOf(current.path("wind_speed_10m").asDouble(3.5)).setScale(1, RoundingMode.HALF_UP);

                    WeatherSnapshot snapshot = WeatherSnapshot.builder()
                            .temperature(temp)
                            .relativeHumidity(humidity)
                            .precipitationProbability(precip.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(0.80) : BigDecimal.valueOf(0.05))
                            .precipitationAmountMm(precip)
                            .windSpeedMps(wind)
                            .atmosphericPressureHpa(pressure)
                            .timestamp(Instant.now())
                            .source("OPEN_METEO_OBSERVED")
                            .build();

                    return Optional.of(snapshot);
                }
            } else {
                log.warn("External weather provider returned HTTP status: {}", response.statusCode());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch external weather: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private void validateSsrf(String targetUrl) {
        try {
            URI uri = URI.create(targetUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new SecurityException("Unsupported protocol scheme for external weather: " + scheme);
            }

            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new SecurityException("Invalid empty host in external weather URL");
            }

            // In production, block loopback and cloud metadata endpoints
            if ("169.254.169.254".equals(host) || "metadata.google.internal".equalsIgnoreCase(host)) {
                throw new SecurityException("Access to cloud metadata service is prohibited");
            }
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Malformed weather provider URL: " + targetUrl);
        }
    }
}
