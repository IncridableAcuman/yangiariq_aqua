package com.aquaai.prediction.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Observed empirical historical performance of a specific prediction method version
 * under an explicitly defined context slice (crop, soil, irrigation method, telemetry quality, season).
 * <p>
 * Strictly represents observed evidence without claiming causality.
 */
@Builder
public record ContextPerformanceProfile(
        PredictionType predictionType,
        String versionTag,
        String cropType,
        String soilType,
        String irrigationMethod,
        TelemetryQualityLevel telemetryQuality,
        AgriculturalSeason season,
        int sampleSize,
        boolean hasSufficientSample,
        BigDecimal meanAbsoluteError,
        BigDecimal rootMeanSquaredError,
        BigDecimal meanBiasError,
        BigDecimal accuracyPercent,
        ModelHealthStatus globalModelHealth,
        ModelHealthStatus contextualModelHealth,
        boolean contextDriftDetected,
        String driftDiagnosis
) {
    public static final int MIN_CONTEXT_SAMPLE_SIZE = 10;

    public static ContextPerformanceProfile create(
            PredictionType predictionType,
            String versionTag,
            String cropType,
            String soilType,
            String irrigationMethod,
            TelemetryQualityLevel telemetryQuality,
            AgriculturalSeason season,
            int sampleSize,
            BigDecimal mae,
            BigDecimal rmse,
            BigDecimal mbe,
            BigDecimal accuracy,
            ModelHealthStatus globalHealth
    ) {
        boolean sufficient = sampleSize >= MIN_CONTEXT_SAMPLE_SIZE;

        ModelHealthStatus contextualHealth = ModelHealthStatus.INSUFFICIENT_DATA;
        boolean drift = false;
        String diagnosis = null;

        if (sufficient) {
            // Contextual health derivation
            if (mae != null) {
                // If MAE is elevated or MBE has high directional bias
                if (mbe != null && mbe.abs().compareTo(BigDecimal.valueOf(10.0)) > 0) {
                    contextualHealth = ModelHealthStatus.DEGRADED;
                    drift = true;
                    diagnosis = String.format("Persistent contextual bias detected (%s%.1f%%) for %s / %s",
                            mbe.compareTo(BigDecimal.ZERO) > 0 ? "+" : "", mbe, cropType, irrigationMethod);
                } else if (mae.compareTo(BigDecimal.valueOf(15.0)) > 0) {
                    contextualHealth = ModelHealthStatus.WATCH;
                    diagnosis = String.format("Elevated MAE (%.1f%%) observed for %s", mae, cropType);
                } else {
                    contextualHealth = ModelHealthStatus.HEALTHY;
                }
            } else if (accuracy != null) {
                if (accuracy.compareTo(BigDecimal.valueOf(70.0)) < 0) {
                    contextualHealth = ModelHealthStatus.DEGRADED;
                    drift = true;
                    diagnosis = String.format("Contextual accuracy dropped to %.1f%% for %s", accuracy, cropType);
                } else if (accuracy.compareTo(BigDecimal.valueOf(80.0)) < 0) {
                    contextualHealth = ModelHealthStatus.WATCH;
                } else {
                    contextualHealth = ModelHealthStatus.HEALTHY;
                }
            }
        }

        return ContextPerformanceProfile.builder()
                .predictionType(predictionType)
                .versionTag(versionTag)
                .cropType(cropType != null ? cropType.toUpperCase() : "ALL")
                .soilType(soilType != null ? soilType.toUpperCase() : "ALL")
                .irrigationMethod(irrigationMethod != null ? irrigationMethod.toUpperCase() : "ALL")
                .telemetryQuality(telemetryQuality != null ? telemetryQuality : TelemetryQualityLevel.UNKNOWN)
                .season(season != null ? season : AgriculturalSeason.SUMMER)
                .sampleSize(sampleSize)
                .hasSufficientSample(sufficient)
                .meanAbsoluteError(mae != null ? mae.setScale(2, RoundingMode.HALF_UP) : null)
                .rootMeanSquaredError(rmse != null ? rmse.setScale(2, RoundingMode.HALF_UP) : null)
                .meanBiasError(mbe != null ? mbe.setScale(2, RoundingMode.HALF_UP) : null)
                .accuracyPercent(accuracy != null ? accuracy.setScale(1, RoundingMode.HALF_UP) : null)
                .globalModelHealth(globalHealth != null ? globalHealth : ModelHealthStatus.HEALTHY)
                .contextualModelHealth(contextualHealth)
                .contextDriftDetected(drift)
                .driftDiagnosis(diagnosis)
                .build();
    }
}
