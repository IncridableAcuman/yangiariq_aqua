package com.aquaai.integration.infrastructure.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalWeatherClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Should return empty optional when external weather integration is disabled")
    void shouldReturnEmptyWhenDisabled() {
        ExternalWeatherProperties props = new ExternalWeatherProperties();
        props.setEnabled(false);

        ExternalWeatherClient client = new ExternalWeatherClient(props, objectMapper);
        var snapshot = client.fetchCurrentWeather(41.3328, 60.5986);

        assertThat(snapshot).isEmpty();
    }

    @Test
    @DisplayName("Should safely handle cloud metadata endpoint SSRF attempts")
    void shouldSafelyHandleCloudMetadataSsrf() {
        ExternalWeatherProperties props = new ExternalWeatherProperties();
        props.setEnabled(true);
        props.setUrl("http://169.254.169.254/latest/meta-data");

        ExternalWeatherClient client = new ExternalWeatherClient(props, objectMapper);
        var snapshot = client.fetchCurrentWeather(41.3328, 60.5986);

        assertThat(snapshot).isEmpty();
    }

    @Test
    @DisplayName("Should safely reject unsupported file or ftp schemes")
    void shouldRejectUnsupportedSchemes() {
        ExternalWeatherProperties props = new ExternalWeatherProperties();
        props.setEnabled(true);
        props.setUrl("file:///etc/passwd");

        ExternalWeatherClient client = new ExternalWeatherClient(props, objectMapper);
        var snapshot = client.fetchCurrentWeather(41.3328, 60.5986);

        assertThat(snapshot).isEmpty();
    }
}
