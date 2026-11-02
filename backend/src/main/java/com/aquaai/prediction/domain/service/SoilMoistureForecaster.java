package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WeatherContext;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Domain service forecasting soil moisture trajectory based on recent telemetry observations,
 * linear depletion velocity, crop evapotranspiration demand, and uncertainty margins.
 */
public class SoilMoistureForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            List<TelemetryObservation> observations,
            WeatherContext weather,
            SufficiencyEvaluation sufficiency,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        if (sufficiency.sufficiency() == DataSufficiency.INSUFFICIENT) {
            return Prediction.createUnavailable(
                    field.fieldId(),
                    PredictionType.SOIL_MOISTURE_FORECAST,
                    horizon,
                    "Soil moisture forecast unavailable: " + sufficiency.summaryExplanation()
            );
        }

        List<TelemetryObservation> validObs = observations.stream()
                .filter(o -> !"INVALID".equalsIgnoreCase(o.quality()))
                .sorted(Comparator.comparing(TelemetryObservation::measuredAt))
                .toList();

        if (validObs.size() < 2) {
            return Prediction.createUnavailable(
                    field.fieldId(),
                    PredictionType.SOIL_MOISTURE_FORECAST,
                    horizon,
                    "Soil moisture forecast unavailable: fewer than 2 valid observations to establish trend slope."
            );
        }

        TelemetryObservation latestObs = validObs.get(validObs.size() - 1);
        BigDecimal currentMoisture = latestObs.value().setScale(1, RoundingMode.HALF_UP);

        // Linear regression slope (% per hour)
        double firstEpochSec = validObs.get(0).measuredAt().getEpochSecond();
        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        int n = validObs.size();

        for (TelemetryObservation o : validObs) {
            double xHours = (o.measuredAt().getEpochSecond() - firstEpochSec) / 3600.0;
            double yVal = o.value().doubleValue();
            sumX += xHours;
            sumY += yVal;
            sumXX += xHours * xHours;
            sumXY += xHours * yVal;
        }

        double denominator = (n * sumXX - sumX * sumX);
        double slopePerHour = 0.0;
        if (Math.abs(denominator) > 1e-6) {
            slopePerHour = (n * sumXY - sumX * sumY) / denominator;
        }

        // Clamp slope to realistic agronomic limits: -0.45% / hr to +0.80% / hr (e.g. after irrigation)
        if (slopePerHour < -0.45) slopePerHour = -0.45;
        if (slopePerHour > 0.80) slopePerHour = 0.80;

        // ET0 adjustment if slope is near 0 or slightly positive without recent irrigation
        double et0 = (weather != null && weather.referenceEt0MmDay() != null)
                ? weather.referenceEt0MmDay().doubleValue()
                : 5.0; // Default summer ET0 mm/day
        double expectedDailyEtcLoss = (et0 * 0.90) / 10.0; // approx % soil moisture loss per day in active root zone
        double expectedEtcPerHour = -(expectedDailyEtcLoss / 24.0);

        // Blend regression slope with expected ETc depletion
        double blendedSlopePerHour;
        if (slopePerHour < 0) {
            blendedSlopePerHour = (slopePerHour * 0.70) + (expectedEtcPerHour * 0.30);
        } else {
            blendedSlopePerHour = slopePerHour;
        }

        int horizonHours = horizon.getHours();
        double deltaMoisture = blendedSlopePerHour * horizonHours;
        double forecastMoistureVal = Math.max(5.0, Math.min(65.0, currentMoisture.doubleValue() + deltaMoisture));

        BigDecimal predictedValue = BigDecimal.valueOf(forecastMoistureVal).setScale(1, RoundingMode.HALF_UP);

        // Confidence adjustment
        int confidence = sufficiency.baseConfidencePercent();
        if (horizon == PredictionHorizon.NEXT_48_HOURS) {
            confidence -= 6;
        } else if (horizon == PredictionHorizon.NEXT_7_DAYS) {
            confidence -= 16;
        }
        confidence = Math.max(30, Math.min(92, confidence));

        // Margin of uncertainty
        double uncertaintySpread = (horizonHours / 24.0) * 1.5 * (1.1 - (confidence / 100.0));
        uncertaintySpread = Math.max(1.5, Math.min(8.0, uncertaintySpread));

        BigDecimal lowerBound = BigDecimal.valueOf(Math.max(5.0, forecastMoistureVal - uncertaintySpread)).setScale(1, RoundingMode.HALF_UP);
        BigDecimal upperBound = BigDecimal.valueOf(Math.min(65.0, forecastMoistureVal + uncertaintySpread)).setScale(1, RoundingMode.HALF_UP);

        // Risk classification
        RiskLevel riskLevel;
        if (forecastMoistureVal < 18.0) {
            riskLevel = RiskLevel.CRITICAL;
        } else if (forecastMoistureVal < 24.0) {
            riskLevel = RiskLevel.HIGH;
        } else if (forecastMoistureVal < 30.0) {
            riskLevel = RiskLevel.MEDIUM;
        } else {
            riskLevel = RiskLevel.LOW;
        }

        String evidence = String.format(
                "Current moisture is %.1f%%. Observed rate of change is %.3f%%/hr based on %d valid observations. Weather reference ET0 is %.1f mm/day.",
                currentMoisture.doubleValue(), blendedSlopePerHour, n, et0
        );

        String assumptions = String.format(
                "Forecast assumes no unannounced irrigation events and steady reference evapotranspiration (%.1f mm/day).",
                et0
        );

        String recommendedAction = riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH
                ? "Prepare irrigation event; field is projected to enter critical moisture stress."
                : riskLevel == RiskLevel.MEDIUM
                ? "Monitor soil moisture trend; irrigation may be needed within 48 to 72 hours."
                : "No immediate irrigation required. Soil moisture remains within optimal agronomic bounds.";

        return Prediction.create(
                field.fieldId(),
                PredictionType.SOIL_MOISTURE_FORECAST,
                horizon,
                targetTime,
                predictedValue,
                lowerBound,
                upperBound,
                "%",
                confidence,
                riskLevel,
                sufficiency.sufficiency(),
                null,
                evidence,
                assumptions,
                recommendedAction,
                "irrigation"
        );
    }
}
