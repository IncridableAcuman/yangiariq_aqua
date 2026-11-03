package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.AnomalySeverityContext;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.model.SensorAvailabilityStatus;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import com.aquaai.prediction.domain.model.WaterAvailabilityContext;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContextNormalizerTest {

    private ContextNormalizer normalizer;
    private UUID fieldId;
    private Instant now;

    @BeforeEach
    void setUp() {
        normalizer = new ContextNormalizer();
        fieldId = UUID.randomUUID();
        now = Instant.parse("2026-07-15T12:00:00Z"); // Summer
    }

    @Test
    @DisplayName("Normalizes complete field operational context")
    void normalizesCompleteContext() {
        FieldMetadata field = new FieldMetadata(
                fieldId, UUID.randomUUID(), "Parcel 4", "COTTON", "SANDY_LOAM", "DRIP", BigDecimal.valueOf(10.0), true
        );

        List<TelemetryObservation> observations = List.of(
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(24.5), "VALID", now.minusSeconds(3600)),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(25.0), "VALID", now.minusSeconds(7200))
        );

        SensorHealthSummary sensor = new SensorHealthSummary(
                UUID.randomUUID(), fieldId, "Soil 1", "ACTIVE", now, 100, 95, 3, 2, 0, 0
        );

        WaterBalance balance = new WaterBalance(
                UUID.randomUUID(), BigDecimal.valueOf(10000), BigDecimal.valueOf(4000), BigDecimal.valueOf(6000), BigDecimal.ZERO, false
        );

        WeatherContext weather = new WeatherContext(
                BigDecimal.valueOf(38.0), BigDecimal.valueOf(25.0), BigDecimal.valueOf(7.2), BigDecimal.ZERO
        );

        PredictionContext ctx = normalizer.normalize(
                field, observations, List.of(sensor), balance, Collections.emptyList(), weather, now
        );

        assertThat(ctx.fieldId()).isEqualTo(fieldId);
        assertThat(ctx.cropType()).isEqualTo("COTTON");
        assertThat(ctx.soilType()).isEqualTo("SANDY_LOAM");
        assertThat(ctx.irrigationMethod()).isEqualTo("DRIP");
        assertThat(ctx.telemetryQuality()).isEqualTo(TelemetryQualityLevel.HIGH);
        assertThat(ctx.sensorAvailability()).isEqualTo(SensorAvailabilityStatus.COMPLETE);
        assertThat(ctx.waterAvailability()).isEqualTo(WaterAvailabilityContext.ABUNDANT);
        assertThat(ctx.recentAnomalySeverity()).isEqualTo(AnomalySeverityContext.NONE);
        assertThat(ctx.season()).isEqualTo(AgriculturalSeason.SUMMER);
    }

    @Test
    @DisplayName("Detects missing sensors and marks sensor availability as MISSING")
    void detectsMissingSensors() {
        FieldMetadata field = new FieldMetadata(
                fieldId, UUID.randomUUID(), "Parcel 9", "WHEAT", "LOAM", "FLOOD", BigDecimal.valueOf(5.0), true
        );

        PredictionContext ctx = normalizer.normalize(
                field, Collections.emptyList(), Collections.emptyList(), null, Collections.emptyList(), null, now
        );

        assertThat(ctx.sensorAvailability()).isEqualTo(SensorAvailabilityStatus.MISSING);
        assertThat(ctx.telemetryQuality()).isEqualTo(TelemetryQualityLevel.INSUFFICIENT);
    }

    @Test
    @DisplayName("Detects active critical hydraulic anomalies")
    void detectsCriticalAnomalies() {
        FieldMetadata field = new FieldMetadata(
                fieldId, UUID.randomUUID(), "Parcel 2", "COTTON", "CLAY", "DRIP", BigDecimal.valueOf(8.0), true
        );

        HydraulicAnomalyInfo anomaly = new HydraulicAnomalyInfo(
                UUID.randomUUID(), fieldId, "PIPE_BURST", "CRITICAL", now.minusSeconds(600), false
        );

        PredictionContext ctx = normalizer.normalize(
                field, Collections.emptyList(), Collections.emptyList(), null, List.of(anomaly), null, now
        );

        assertThat(ctx.recentAnomalySeverity()).isEqualTo(AnomalySeverityContext.CRITICAL);
    }
}
