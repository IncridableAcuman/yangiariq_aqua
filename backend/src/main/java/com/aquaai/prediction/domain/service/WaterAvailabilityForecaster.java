package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Domain service evaluating water quota availability risk by comparing forecast demand
 * against remaining seasonal allocation balances.
 */
public class WaterAvailabilityForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            Prediction waterDemandForecast,
            WaterBalance waterBalance,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        if (waterBalance == null || waterBalance.allocatedVolumeM3() == null) {
            return Prediction.createUnavailable(
                    field.fieldId(),
                    PredictionType.WATER_AVAILABILITY_RISK,
                    horizon,
                    "Water availability risk evaluation unavailable: no active water allocation record linked to this field."
            );
        }

        BigDecimal demandM3 = (waterDemandForecast != null && waterDemandForecast.getPredictedValue() != null)
                ? waterDemandForecast.getPredictedValue()
                : BigDecimal.ZERO;

        BigDecimal remainingM3 = waterBalance.remainingVolumeM3() != null
                ? waterBalance.remainingVolumeM3()
                : BigDecimal.ZERO;

        BigDecimal allocatedM3 = waterBalance.allocatedVolumeM3();
        BigDecimal consumedM3 = waterBalance.consumedVolumeM3() != null ? waterBalance.consumedVolumeM3() : BigDecimal.ZERO;

        RiskLevel risk;
        BigDecimal deficitM3 = BigDecimal.ZERO;

        if (waterBalance.isOverConsumed() || consumedM3.compareTo(allocatedM3) >= 0) {
            risk = RiskLevel.CRITICAL;
            deficitM3 = demandM3;
        } else if (demandM3.compareTo(remainingM3) > 0) {
            risk = RiskLevel.HIGH;
            deficitM3 = demandM3.subtract(remainingM3);
        } else if (remainingM3.subtract(demandM3).compareTo(allocatedM3.multiply(BigDecimal.valueOf(0.15))) < 0) {
            risk = RiskLevel.MEDIUM; // Watch
        } else {
            risk = RiskLevel.LOW;
        }

        int confidence = waterDemandForecast != null ? waterDemandForecast.getConfidencePercent() : 80;

        String evidence = String.format(
                "Forecast demand: %s m³, Remaining quota balance: %s m³ (Allocated: %s m³, Consumed: %s m³).",
                demandM3.toPlainString(), remainingM3.toPlainString(), allocatedM3.toPlainString(), consumedM3.toPlainString()
        );

        String assumptions = "Assumes no quota top-up or secondary canal transfers before target horizon.";

        String recommendedAction = risk == RiskLevel.CRITICAL || risk == RiskLevel.HIGH
                ? String.format("Request water allocation quota increase; forecast demand exceeds remaining volume by ~%s m³.", deficitM3.toPlainString())
                : risk == RiskLevel.MEDIUM
                ? "Monitor water consumption closely; quota buffer is approaching low threshold (<15%)."
                : "Water quota balance is adequate for projected irrigation demand.";

        return Prediction.create(
                field.fieldId(),
                PredictionType.WATER_AVAILABILITY_RISK,
                horizon,
                targetTime,
                deficitM3.setScale(0, RoundingMode.HALF_UP),
                BigDecimal.ZERO,
                deficitM3.multiply(BigDecimal.valueOf(1.20)).setScale(0, RoundingMode.HALF_UP),
                "m³",
                confidence,
                risk,
                waterDemandForecast != null ? waterDemandForecast.getDataSufficiency() : DataSufficiency.SUFFICIENT,
                null,
                evidence,
                assumptions,
                recommendedAction,
                "water"
        );
    }
}
