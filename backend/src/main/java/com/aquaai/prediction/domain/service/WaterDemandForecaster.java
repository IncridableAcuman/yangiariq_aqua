package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.UpcomingPlanInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

/**
 * Domain service forecasting projected volumetric water demand over a given horizon,
 * integrating upcoming scheduled plans and predicted deficit replenishment.
 */
public class WaterDemandForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            Prediction irrigationNeedForecast,
            List<UpcomingPlanInfo> upcomingPlans,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        BigDecimal scheduledVolume = BigDecimal.ZERO;
        int scheduledCount = 0;

        if (upcomingPlans != null) {
            for (UpcomingPlanInfo plan : upcomingPlans) {
                if (plan.plannedStartAt() != null && !plan.plannedStartAt().isAfter(targetTime) && !plan.isBlocked()) {
                    if (plan.recommendedVolumeM3() != null) {
                        scheduledVolume = scheduledVolume.add(plan.recommendedVolumeM3());
                        scheduledCount++;
                    }
                }
            }
        }

        BigDecimal predictedNeedVolume = BigDecimal.ZERO;
        DataSufficiency sufficiency = DataSufficiency.SUFFICIENT;
        int confidence = 85;

        if (irrigationNeedForecast != null) {
            sufficiency = irrigationNeedForecast.getDataSufficiency();
            confidence = irrigationNeedForecast.getConfidencePercent();
            if (irrigationNeedForecast.getPredictedValue() != null &&
                irrigationNeedForecast.getLikelihood() != IrrigationLikelihood.NOT_EXPECTED &&
                irrigationNeedForecast.getLikelihood() != IrrigationLikelihood.UNKNOWN) {
                // If plans are already scheduled, avoid double counting
                if (scheduledVolume.compareTo(BigDecimal.ZERO) == 0) {
                    predictedNeedVolume = irrigationNeedForecast.getPredictedValue();
                }
            }
        }

        BigDecimal totalExpectedDemand = scheduledVolume.add(predictedNeedVolume).setScale(0, RoundingMode.HALF_UP);
        BigDecimal lowerBound = totalExpectedDemand.multiply(BigDecimal.valueOf(0.85)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal upperBound = totalExpectedDemand.multiply(BigDecimal.valueOf(1.15)).setScale(0, RoundingMode.HALF_UP);

        RiskLevel risk = totalExpectedDemand.compareTo(BigDecimal.ZERO) > 0 ? RiskLevel.MEDIUM : RiskLevel.LOW;

        String evidence = String.format(
                "Projected water demand is %s m³ (%s scheduled across %d plans, %s estimated from agronomic deficit).",
                totalExpectedDemand.toPlainString(), scheduledVolume.toPlainString(), scheduledCount, predictedNeedVolume.toPlainString()
        );

        String assumptions = String.format(
                "Demand forecast reflects delivery schedule and root-zone deficit for %s ha over %s.",
                field.areaHectares() != null ? field.areaHectares().toPlainString() : "1.0",
                horizon.getLabel()
        );

        String recommendedAction = totalExpectedDemand.compareTo(BigDecimal.ZERO) > 0
                ? String.format("Verify water distribution channel capacity to supply ~%s m³.", totalExpectedDemand.toPlainString())
                : "No active water delivery scheduled or predicted.";

        return Prediction.create(
                field.fieldId(),
                PredictionType.WATER_DEMAND_FORECAST,
                horizon,
                targetTime,
                totalExpectedDemand,
                lowerBound,
                upperBound,
                "m³",
                confidence,
                risk,
                sufficiency,
                null,
                evidence,
                assumptions,
                recommendedAction,
                "water"
        );
    }
}
