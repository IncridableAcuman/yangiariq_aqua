package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Domain service evaluating whether a parcel is likely to cross its Management Allowed Depletion (MAD)
 * threshold, forecasting irrigation likelihood and estimated volumetric requirement.
 */
public class IrrigationNeedForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            Prediction soilMoistureForecast,
            WeatherContext weather,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        if (soilMoistureForecast == null || soilMoistureForecast.getDataSufficiency() == DataSufficiency.INSUFFICIENT || soilMoistureForecast.getPredictedValue() == null) {
            return Prediction.createUnavailable(
                    field.fieldId(),
                    PredictionType.IRRIGATION_NEED_FORECAST,
                    horizon,
                    "Irrigation need forecast unavailable: insufficient soil-moisture telemetry."
            );
        }

        BigDecimal forecastMoisture = soilMoistureForecast.getPredictedValue();
        BigDecimal targetMoisture = resolveTargetMoisture(field.cropType(), field.soilType());
        BigDecimal madThreshold = resolveMadThreshold(field.cropType());

        // Determine likelihood
        IrrigationLikelihood likelihood;
        RiskLevel riskLevel;

        if (forecastMoisture.compareTo(madThreshold.subtract(BigDecimal.valueOf(3.0))) < 0) {
            likelihood = IrrigationLikelihood.HIGHLY_LIKELY;
            riskLevel = RiskLevel.HIGH;
        } else if (forecastMoisture.compareTo(madThreshold) <= 0) {
            likelihood = (horizon == PredictionHorizon.NEXT_24_HOURS || horizon == PredictionHorizon.NEXT_48_HOURS)
                    ? IrrigationLikelihood.LIKELY
                    : IrrigationLikelihood.POSSIBLE;
            riskLevel = RiskLevel.MEDIUM;
        } else if (forecastMoisture.compareTo(madThreshold.add(BigDecimal.valueOf(4.0))) <= 0) {
            likelihood = (horizon == PredictionHorizon.NEXT_7_DAYS)
                    ? IrrigationLikelihood.POSSIBLE
                    : IrrigationLikelihood.LOW_LIKELIHOOD;
            riskLevel = RiskLevel.LOW;
        } else {
            likelihood = IrrigationLikelihood.NOT_EXPECTED;
            riskLevel = RiskLevel.LOW;
        }

        // Calculate expected volumetric need if irrigating to target moisture
        BigDecimal expectedVolumeM3 = BigDecimal.ZERO;
        BigDecimal lowerBoundM3 = BigDecimal.ZERO;
        BigDecimal upperBoundM3 = BigDecimal.ZERO;

        if (likelihood != IrrigationLikelihood.NOT_EXPECTED) {
            BigDecimal area = field.areaHectares() != null && field.areaHectares().compareTo(BigDecimal.ZERO) > 0
                    ? field.areaHectares()
                    : BigDecimal.ONE;

            BigDecimal rootDepthM = resolveRootDepthMeters(field.cropType());
            BigDecimal efficiency = resolveEfficiency(field.irrigationMethod());

            // Gap in %
            BigDecimal moistureGap = targetMoisture.subtract(forecastMoisture);
            if (moistureGap.compareTo(BigDecimal.ZERO) < 0) {
                moistureGap = BigDecimal.ZERO;
            }

            // Deficit mm = gap / 100 * rootDepth * 1000
            BigDecimal deficitMm = moistureGap.divide(BigDecimal.valueOf(100.0), 4, RoundingMode.HALF_UP)
                    .multiply(rootDepthM).multiply(BigDecimal.valueOf(1000.0));

            // Plus ETc over remaining horizon
            BigDecimal et0 = (weather != null && weather.referenceEt0MmDay() != null)
                    ? weather.referenceEt0MmDay()
                    : BigDecimal.valueOf(5.0);
            BigDecimal kc = resolveCropKc(field.cropType());
            BigDecimal etcTotalMm = kc.multiply(et0).multiply(BigDecimal.valueOf(horizon.getHours() / 24.0));

            BigDecimal netMm = deficitMm.add(etcTotalMm);
            BigDecimal grossM3 = netMm.multiply(BigDecimal.valueOf(10.0)).multiply(area)
                    .divide(efficiency, 0, RoundingMode.HALF_UP);

            expectedVolumeM3 = grossM3;
            lowerBoundM3 = grossM3.multiply(BigDecimal.valueOf(0.85)).setScale(0, RoundingMode.HALF_UP);
            upperBoundM3 = grossM3.multiply(BigDecimal.valueOf(1.15)).setScale(0, RoundingMode.HALF_UP);
        }

        int confidence = Math.max(30, soilMoistureForecast.getConfidencePercent() - 4);

        String evidence = String.format(
                "Forecast moisture is %.1f%% against MAD stress threshold %.1f%% and target %.1f%% for crop %s.",
                forecastMoisture.doubleValue(), madThreshold.doubleValue(), targetMoisture.doubleValue(),
                field.cropType() != null ? field.cropType() : "COTTON"
        );

        String assumptions = String.format(
                "Volumetric estimation based on %.1f ha area, %s irrigation method with %.0f%% application efficiency.",
                field.areaHectares() != null ? field.areaHectares().doubleValue() : 1.0,
                field.irrigationMethod() != null ? field.irrigationMethod() : "DRIP",
                resolveEfficiency(field.irrigationMethod()).doubleValue() * 100
        );

        String recommendedAction = (likelihood == IrrigationLikelihood.HIGHLY_LIKELY || likelihood == IrrigationLikelihood.LIKELY)
                ? String.format("Review smart irrigation planning: field likely requires ~%s m³ within %s.",
                expectedVolumeM3.toPlainString(), horizon.getLabel())
                : "No immediate plan scheduling required.";

        return Prediction.create(
                field.fieldId(),
                PredictionType.IRRIGATION_NEED_FORECAST,
                horizon,
                targetTime,
                expectedVolumeM3,
                lowerBoundM3,
                upperBoundM3,
                "m³",
                confidence,
                riskLevel,
                soilMoistureForecast.getDataSufficiency(),
                likelihood,
                evidence,
                assumptions,
                recommendedAction,
                "irrigation"
        );
    }

    private BigDecimal resolveTargetMoisture(String cropType, String soilType) {
        if (cropType == null) return BigDecimal.valueOf(32.0);
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(32.0);
            case "WHEAT" -> BigDecimal.valueOf(30.0);
            case "RICE" -> BigDecimal.valueOf(45.0);
            case "MAIZE" -> BigDecimal.valueOf(33.0);
            case "VEGETABLE" -> BigDecimal.valueOf(34.0);
            case "MELON" -> BigDecimal.valueOf(28.0);
            default -> BigDecimal.valueOf(30.0);
        };
    }

    private BigDecimal resolveMadThreshold(String cropType) {
        if (cropType == null) return BigDecimal.valueOf(24.0);
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(23.0);
            case "WHEAT" -> BigDecimal.valueOf(22.0);
            case "RICE" -> BigDecimal.valueOf(35.0);
            case "MAIZE" -> BigDecimal.valueOf(24.0);
            case "VEGETABLE" -> BigDecimal.valueOf(26.0);
            case "MELON" -> BigDecimal.valueOf(20.0);
            default -> BigDecimal.valueOf(22.0);
        };
    }

    private BigDecimal resolveCropKc(String cropType) {
        if (cropType == null) return BigDecimal.valueOf(0.85);
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(0.95);
            case "WHEAT" -> BigDecimal.valueOf(0.85);
            case "RICE" -> BigDecimal.valueOf(1.15);
            case "MAIZE" -> BigDecimal.valueOf(0.90);
            case "VEGETABLE" -> BigDecimal.valueOf(0.80);
            case "MELON" -> BigDecimal.valueOf(0.75);
            default -> BigDecimal.valueOf(0.85);
        };
    }

    private BigDecimal resolveRootDepthMeters(String cropType) {
        if (cropType == null) return BigDecimal.valueOf(0.60);
        return switch (cropType.toUpperCase()) {
            case "COTTON" -> BigDecimal.valueOf(0.80);
            case "WHEAT" -> BigDecimal.valueOf(0.70);
            case "RICE" -> BigDecimal.valueOf(0.40);
            case "MAIZE" -> BigDecimal.valueOf(0.75);
            case "VEGETABLE" -> BigDecimal.valueOf(0.40);
            case "MELON" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.valueOf(0.60);
        };
    }

    private BigDecimal resolveEfficiency(String irrigationMethod) {
        if (irrigationMethod == null) return BigDecimal.valueOf(0.80);
        return switch (irrigationMethod.toUpperCase()) {
            case "DRIP" -> BigDecimal.valueOf(0.90);
            case "SPRINKLER" -> BigDecimal.valueOf(0.75);
            case "FURROW" -> BigDecimal.valueOf(0.60);
            case "CANAL", "FLOOD" -> BigDecimal.valueOf(0.50);
            default -> BigDecimal.valueOf(0.75);
        };
    }
}
