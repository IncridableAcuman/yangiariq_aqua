package com.aquaai.integration.infrastructure.weather;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.weather.external")
public class ExternalWeatherProperties {

    /**
     * Whether external weather API integration is enabled.
     * When false, system operates with SimulatedYangiariqWeatherProvider.
     */
    private boolean enabled = false;

    /**
     * Base URL for the external weather service (e.g. Open-Meteo or national agro-meteorology API).
     */
    private String url = "https://api.open-meteo.com/v1/forecast";

    /**
     * Optional API secret token (never logged or exposed via API).
     */
    private String apiKey = "";

    /**
     * HTTP request timeout in milliseconds.
     */
    private int timeoutMs = 5000;

    /**
     * Default geographic coordinates for Yangiariq district center.
     */
    private double districtLatitude = 41.3328;
    private double districtLongitude = 60.5986;

    /**
     * Max retry attempts on transient network failures.
     */
    private int maxRetries = 2;
}
