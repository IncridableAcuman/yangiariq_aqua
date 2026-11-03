package com.aquaai.prediction.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable operational context describing parcel agronomic metadata, physical environment,
 * telemetry health, and seasonal state for adaptive method selection.
 */
@Builder
public record PredictionContext(
        UUID fieldId,
        String cropType,
        String soilType,
        String irrigationMethod,
        BigDecimal fieldAreaHectares,
        AgriculturalSeason season,
        TelemetryQualityLevel telemetryQuality,
        SensorAvailabilityStatus sensorAvailability,
        WaterAvailabilityContext waterAvailability,
        AnomalySeverityContext recentAnomalySeverity,
        String temperatureContext,
        String rainfallContext,
        Instant capturedAt
) {
    public PredictionContext {
        Objects.requireNonNull(fieldId, "fieldId must not be null");
        cropType = cropType != null ? cropType.toUpperCase() : "UNKNOWN";
        soilType = soilType != null ? soilType.toUpperCase() : "UNKNOWN";
        irrigationMethod = irrigationMethod != null ? irrigationMethod.toUpperCase() : "UNKNOWN";
        fieldAreaHectares = fieldAreaHectares != null ? fieldAreaHectares : BigDecimal.ZERO;
        season = season != null ? season : AgriculturalSeason.SUMMER;
        telemetryQuality = telemetryQuality != null ? telemetryQuality : TelemetryQualityLevel.UNKNOWN;
        sensorAvailability = sensorAvailability != null ? sensorAvailability : SensorAvailabilityStatus.UNKNOWN;
        waterAvailability = waterAvailability != null ? waterAvailability : WaterAvailabilityContext.UNKNOWN;
        recentAnomalySeverity = recentAnomalySeverity != null ? recentAnomalySeverity : AnomalySeverityContext.NONE;
        temperatureContext = temperatureContext != null ? temperatureContext : "UNKNOWN";
        rainfallContext = rainfallContext != null ? rainfallContext : "UNKNOWN";
        capturedAt = capturedAt != null ? capturedAt : Instant.now();
    }

    public static PredictionContext createUnknown(UUID fieldId) {
        return PredictionContext.builder()
                .fieldId(fieldId)
                .cropType("UNKNOWN")
                .soilType("UNKNOWN")
                .irrigationMethod("UNKNOWN")
                .fieldAreaHectares(BigDecimal.ZERO)
                .season(AgriculturalSeason.SUMMER)
                .telemetryQuality(TelemetryQualityLevel.UNKNOWN)
                .sensorAvailability(SensorAvailabilityStatus.UNKNOWN)
                .waterAvailability(WaterAvailabilityContext.UNKNOWN)
                .recentAnomalySeverity(AnomalySeverityContext.NONE)
                .temperatureContext("UNKNOWN")
                .rainfallContext("UNKNOWN")
                .capturedAt(Instant.now())
                .build();
    }

    /**
     * Determines whether the parcel context has sufficient metadata for specialized matching.
     */
    public boolean hasSufficientAgronomicMetadata() {
        return !"UNKNOWN".equals(cropType) && !"UNKNOWN".equals(irrigationMethod);
    }
}
