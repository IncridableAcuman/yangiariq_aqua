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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Deterministic normalizer mapping raw telemetry, field metadata, anomalies, and water balances
 * into standardized, categorical PredictionContext dimensions.
 */
public class ContextNormalizer {

    private static final ZoneId UZBEKISTAN_ZONE = ZoneId.of("Asia/Tashkent");

    public PredictionContext normalize(
            FieldMetadata field,
            List<TelemetryObservation> recentTelemetry,
            List<SensorHealthSummary> sensors,
            WaterBalance waterBalance,
            List<HydraulicAnomalyInfo> activeAnomalies,
            WeatherContext weather,
            Instant now
    ) {
        if (field == null) {
            return PredictionContext.createUnknown(java.util.UUID.randomUUID());
        }

        Instant evaluatedAt = now != null ? now : Instant.now();

        // 1. Season derivation
        AgriculturalSeason season = AgriculturalSeason.fromDate(evaluatedAt.atZone(UZBEKISTAN_ZONE).toLocalDate());

        // 2. Telemetry Quality Normalization
        TelemetryQualityLevel telemetryQuality = deriveTelemetryQuality(recentTelemetry);

        // 3. Sensor Availability Normalization
        SensorAvailabilityStatus sensorAvailability = deriveSensorAvailability(sensors);

        // 4. Water Availability Normalization
        WaterAvailabilityContext waterAvailability = deriveWaterAvailability(waterBalance);

        // 5. Recent Anomaly Severity Normalization
        AnomalySeverityContext anomalySeverity = deriveAnomalySeverity(activeAnomalies);

        // 6. Temperature and Rainfall Context
        String tempContext = deriveTemperatureContext(weather);
        String rainContext = deriveRainfallContext(weather);

        return PredictionContext.builder()
                .fieldId(field.fieldId())
                .cropType(field.cropType() != null ? field.cropType().toUpperCase() : "UNKNOWN")
                .soilType(field.soilType() != null ? field.soilType().toUpperCase() : "UNKNOWN")
                .irrigationMethod(field.irrigationMethod() != null ? field.irrigationMethod().toUpperCase() : "UNKNOWN")
                .fieldAreaHectares(field.areaHectares() != null ? field.areaHectares() : BigDecimal.ZERO)
                .season(season)
                .telemetryQuality(telemetryQuality)
                .sensorAvailability(sensorAvailability)
                .waterAvailability(waterAvailability)
                .recentAnomalySeverity(anomalySeverity)
                .temperatureContext(tempContext)
                .rainfallContext(rainContext)
                .capturedAt(evaluatedAt)
                .build();
    }

    private TelemetryQualityLevel deriveTelemetryQuality(List<TelemetryObservation> obs) {
        if (obs == null || obs.isEmpty()) {
            return TelemetryQualityLevel.INSUFFICIENT;
        }

        long validCount = obs.stream()
                .filter(o -> "VALID".equalsIgnoreCase(o.quality()))
                .count();

        double ratio = (double) validCount / obs.size();
        if (ratio >= 0.85) return TelemetryQualityLevel.HIGH;
        if (ratio >= 0.60) return TelemetryQualityLevel.MEDIUM;
        if (ratio >= 0.30) return TelemetryQualityLevel.LOW;
        return TelemetryQualityLevel.INSUFFICIENT;
    }

    private SensorAvailabilityStatus deriveSensorAvailability(List<SensorHealthSummary> sensors) {
        if (sensors == null || sensors.isEmpty()) {
            return SensorAvailabilityStatus.MISSING;
        }

        boolean anyOffline = sensors.stream()
                .anyMatch(s -> "OFFLINE".equalsIgnoreCase(s.status()) || s.offlinePeriodsCount() > 2);

        if (anyOffline) {
            return SensorAvailabilityStatus.PARTIAL;
        }
        return SensorAvailabilityStatus.COMPLETE;
    }

    private WaterAvailabilityContext deriveWaterAvailability(WaterBalance balance) {
        if (balance == null) {
            return WaterAvailabilityContext.UNKNOWN;
        }

        if (balance.isOverConsumed()) {
            return WaterAvailabilityContext.CRITICAL;
        }

        if (balance.allocatedVolumeM3() == null || balance.allocatedVolumeM3().compareTo(BigDecimal.ZERO) <= 0) {
            return WaterAvailabilityContext.UNKNOWN;
        }

        BigDecimal remaining = balance.remainingVolumeM3() != null ? balance.remainingVolumeM3() : BigDecimal.ZERO;
        BigDecimal allocated = balance.allocatedVolumeM3();

        BigDecimal ratio = remaining.divide(allocated, 4, RoundingMode.HALF_UP);

        if (ratio.compareTo(BigDecimal.valueOf(0.05)) <= 0) return WaterAvailabilityContext.CRITICAL;
        if (ratio.compareTo(BigDecimal.valueOf(0.20)) <= 0) return WaterAvailabilityContext.CONSTRAINED;
        if (ratio.compareTo(BigDecimal.valueOf(0.50)) <= 0) return WaterAvailabilityContext.NORMAL;
        return WaterAvailabilityContext.ABUNDANT;
    }

    private AnomalySeverityContext deriveAnomalySeverity(List<HydraulicAnomalyInfo> anomalies) {
        if (anomalies == null || anomalies.isEmpty()) {
            return AnomalySeverityContext.NONE;
        }

        boolean hasCritical = anomalies.stream().anyMatch(a -> !a.isResolved() && "CRITICAL".equalsIgnoreCase(a.severity()));
        if (hasCritical) return AnomalySeverityContext.CRITICAL;

        boolean hasHigh = anomalies.stream().anyMatch(a -> !a.isResolved() && "HIGH".equalsIgnoreCase(a.severity()));
        if (hasHigh) return AnomalySeverityContext.HIGH;

        boolean hasMed = anomalies.stream().anyMatch(a -> !a.isResolved() && "MEDIUM".equalsIgnoreCase(a.severity()));
        if (hasMed) return AnomalySeverityContext.MEDIUM;

        boolean hasLow = anomalies.stream().anyMatch(a -> !a.isResolved());
        if (hasLow) return AnomalySeverityContext.LOW;

        return AnomalySeverityContext.NONE;
    }

    private String deriveTemperatureContext(WeatherContext weather) {
        if (weather == null || weather.temperatureC() == null) return "UNKNOWN";
        double temp = weather.temperatureC().doubleValue();
        if (temp >= 35.0) return "VERY_HOT";
        if (temp >= 25.0) return "HOT";
        if (temp >= 15.0) return "MODERATE";
        return "COOL";
    }

    private String deriveRainfallContext(WeatherContext weather) {
        if (weather == null || weather.forecastRainMm() == null) return "UNKNOWN";
        return weather.forecastRainMm().compareTo(BigDecimal.valueOf(3.0)) >= 0 ? "RAIN_EXPECTED" : "DRY";
    }
}
