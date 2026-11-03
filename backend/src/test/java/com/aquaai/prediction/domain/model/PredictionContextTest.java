package com.aquaai.prediction.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionContextTest {

    @Test
    @DisplayName("Creates normalized prediction context with complete fields")
    void createsNormalizedPredictionContext() {
        UUID fieldId = UUID.randomUUID();
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("COTTON")
                .soilType("SANDY_LOAM")
                .irrigationMethod("DRIP")
                .fieldAreaHectares(BigDecimal.valueOf(12.5))
                .telemetryQuality(TelemetryQualityLevel.HIGH)
                .sensorAvailability(SensorAvailabilityStatus.COMPLETE)
                .waterAvailability(WaterAvailabilityContext.NORMAL)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .season(AgriculturalSeason.SUMMER)
                .temperatureContext("HOT")
                .rainfallContext("DRY")
                .build();

        assertThat(context.fieldId()).isEqualTo(fieldId);
        assertThat(context.cropType()).isEqualTo("COTTON");
        assertThat(context.soilType()).isEqualTo("SANDY_LOAM");
        assertThat(context.irrigationMethod()).isEqualTo("DRIP");
        assertThat(context.telemetryQuality()).isEqualTo(TelemetryQualityLevel.HIGH);
        assertThat(context.sensorAvailability()).isEqualTo(SensorAvailabilityStatus.COMPLETE);
        assertThat(context.waterAvailability()).isEqualTo(WaterAvailabilityContext.NORMAL);
        assertThat(context.recentAnomalySeverity()).isEqualTo(AnomalySeverityContext.NONE);
        assertThat(context.season()).isEqualTo(AgriculturalSeason.SUMMER);
        assertThat(context.temperatureContext()).isEqualTo("HOT");
        assertThat(context.rainfallContext()).isEqualTo("DRY");
        assertThat(context.hasSufficientAgronomicMetadata()).isTrue();
    }

    @Test
    @DisplayName("Handles null and edge case values with default safe fallbacks")
    void handlesNullValuesWithSafeFallbacks() {
        UUID fieldId = UUID.randomUUID();
        PredictionContext context = PredictionContext.builder()
                .fieldId(fieldId)
                .build();

        assertThat(context.fieldId()).isEqualTo(fieldId);
        assertThat(context.cropType()).isEqualTo("UNKNOWN");
        assertThat(context.soilType()).isEqualTo("UNKNOWN");
        assertThat(context.irrigationMethod()).isEqualTo("UNKNOWN");
        assertThat(context.telemetryQuality()).isEqualTo(TelemetryQualityLevel.UNKNOWN);
        assertThat(context.sensorAvailability()).isEqualTo(SensorAvailabilityStatus.UNKNOWN);
        assertThat(context.waterAvailability()).isEqualTo(WaterAvailabilityContext.UNKNOWN);
        assertThat(context.recentAnomalySeverity()).isEqualTo(AnomalySeverityContext.NONE);
        assertThat(context.season()).isEqualTo(AgriculturalSeason.SUMMER);
        assertThat(context.hasSufficientAgronomicMetadata()).isFalse();
    }
}
