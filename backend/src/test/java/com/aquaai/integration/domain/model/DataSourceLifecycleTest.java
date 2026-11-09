package com.aquaai.integration.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSourceLifecycleTest {

    @Test
    @DisplayName("Should create data source and manage lifecycle transitions")
    void shouldManageLifecycleTransitions() {
        DataSource source = DataSource.create(
                "OPEN_METEO_TEST",
                "Open-Meteo Test Service",
                DataSourceType.WEATHER_PROVIDER,
                "OpenMeteoAdapter",
                "Testing weather data source",
                "https://api.open-meteo.com/v1/forecast",
                "CONFIG:test",
                DataSourceStatus.ACTIVE
        );

        assertThat(source.getStatus()).isEqualTo(DataSourceStatus.ACTIVE);
        assertThat(source.getStatus().isOperational()).isTrue();

        source.deactivate();
        assertThat(source.getStatus()).isEqualTo(DataSourceStatus.INACTIVE);
        assertThat(source.getStatus().isOperational()).isFalse();

        source.activate();
        assertThat(source.getStatus()).isEqualTo(DataSourceStatus.ACTIVE);

        source.markDegraded("Transient gateway timeout");
        assertThat(source.getStatus()).isEqualTo(DataSourceStatus.DEGRADED);
        assertThat(source.getLastErrorMessage()).contains("Transient gateway timeout");
    }

    @Test
    @DisplayName("Should sanitize secret credentials from endpoint URLs and error logs")
    void shouldSanitizeCredentialsAndErrors() {
        assertThatThrownBy(() -> DataSource.create(
                "BAD_URL",
                "Bad URL",
                DataSourceType.EXTERNAL_API,
                "Adapter",
                "desc",
                "https://user:secret123@api.weather.com",
                null,
                DataSourceStatus.ACTIVE
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not contain embedded user credentials");

        DataSource source = DataSource.create(
                "SAFE_SOURCE",
                "Safe Source",
                DataSourceType.EXTERNAL_API,
                "Adapter",
                "desc",
                "https://api.weather.com/v1",
                null,
                DataSourceStatus.ACTIVE
        );

        source.recordSyncFailure("Connection failed with token=MY_SUPER_SECRET_TOKEN_XYZ", 1);
        assertThat(source.getLastErrorMessage()).doesNotContain("MY_SUPER_SECRET_TOKEN_XYZ");
        assertThat(source.getLastErrorMessage()).contains("token=[REDACTED]");
    }

    @Test
    @DisplayName("Should validate source code format")
    void shouldValidateSourceCodeFormat() {
        assertThatThrownBy(() -> DataSource.create(
                "bad code with spaces!",
                "Name",
                DataSourceType.SENSOR_DEVICE,
                "Adapter",
                null, null, null, DataSourceStatus.ACTIVE
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
