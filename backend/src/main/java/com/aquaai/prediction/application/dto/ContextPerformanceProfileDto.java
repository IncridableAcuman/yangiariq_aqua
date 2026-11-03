package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.ContextPerformanceProfile;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ContextPerformanceProfileDto(
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
    public static ContextPerformanceProfileDto fromDomain(ContextPerformanceProfile p) {
        if (p == null) return null;
        return ContextPerformanceProfileDto.builder()
                .predictionType(p.predictionType())
                .versionTag(p.versionTag())
                .cropType(p.cropType())
                .soilType(p.soilType())
                .irrigationMethod(p.irrigationMethod())
                .telemetryQuality(p.telemetryQuality())
                .season(p.season())
                .sampleSize(p.sampleSize())
                .hasSufficientSample(p.hasSufficientSample())
                .meanAbsoluteError(p.meanAbsoluteError())
                .rootMeanSquaredError(p.rootMeanSquaredError())
                .meanBiasError(p.meanBiasError())
                .accuracyPercent(p.accuracyPercent())
                .globalModelHealth(p.globalModelHealth())
                .contextualModelHealth(p.contextualModelHealth())
                .contextDriftDetected(p.contextDriftDetected())
                .driftDiagnosis(p.driftDiagnosis())
                .build();
    }
}
