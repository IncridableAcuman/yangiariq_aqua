package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.AgriculturalSeason;
import com.aquaai.prediction.domain.model.AnomalySeverityContext;
import com.aquaai.prediction.domain.model.PredictionContext;
import com.aquaai.prediction.domain.model.SensorAvailabilityStatus;
import com.aquaai.prediction.domain.model.TelemetryQualityLevel;
import com.aquaai.prediction.domain.model.WaterAvailabilityContext;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record PredictionContextDto(
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
    public static PredictionContextDto fromDomain(PredictionContext ctx) {
        if (ctx == null) return null;
        return PredictionContextDto.builder()
                .fieldId(ctx.fieldId())
                .cropType(ctx.cropType())
                .soilType(ctx.soilType())
                .irrigationMethod(ctx.irrigationMethod())
                .fieldAreaHectares(ctx.fieldAreaHectares())
                .season(ctx.season())
                .telemetryQuality(ctx.telemetryQuality())
                .sensorAvailability(ctx.sensorAvailability())
                .waterAvailability(ctx.waterAvailability())
                .recentAnomalySeverity(ctx.recentAnomalySeverity())
                .temperatureContext(ctx.temperatureContext())
                .rainfallContext(ctx.rainfallContext())
                .capturedAt(ctx.capturedAt())
                .build();
    }
}
