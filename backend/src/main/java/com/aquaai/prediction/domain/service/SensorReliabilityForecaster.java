package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.SensorHealthSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

/**
 * Domain service forecasting sensor reliability and telemetry degradation trends over 7-14 days.
 */
public class SensorReliabilityForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            List<SensorHealthSummary> sensorSummaries,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        if (sensorSummaries == null || sensorSummaries.isEmpty()) {
            return Prediction.createUnavailable(
                    field.fieldId(),
                    PredictionType.SENSOR_RELIABILITY_FORECAST,
                    horizon,
                    "Sensor reliability forecast unavailable: no sensors linked to this field."
            );
        }

        long totalReadings = 0;
        long totalValid = 0;
        long totalSuspect = 0;
        long totalInvalid = 0;
        int totalStalePeriods = 0;
        int totalOfflinePeriods = 0;

        for (SensorHealthSummary s : sensorSummaries) {
            totalReadings += s.totalReadings14d();
            totalValid += s.validReadings14d();
            totalSuspect += s.suspectReadings14d();
            totalInvalid += s.invalidReadings14d();
            totalStalePeriods += s.stalePeriodsCount();
            totalOfflinePeriods += s.offlinePeriodsCount();
        }

        if (totalReadings < 10) {
            return Prediction.create(
                    field.fieldId(),
                    PredictionType.SENSOR_RELIABILITY_FORECAST,
                    horizon,
                    targetTime,
                    null,
                    null,
                    null,
                    "%",
                    0,
                    RiskLevel.UNKNOWN,
                    DataSufficiency.INSUFFICIENT,
                    null,
                    "Insufficient 14-day telemetry history to model sensor degradation trends (only " + totalReadings + " readings recorded).",
                    "A minimum of 10 telemetry observations is required.",
                    "Verify field gateway connectivity and device installation.",
                    "sensors"
            );
        }

        double validRatio = (double) totalValid / totalReadings;
        double invalidRatio = (double) (totalSuspect + totalInvalid) / totalReadings;

        RiskLevel risk;
        String statusLabel;
        String action;

        if (totalOfflinePeriods > 3 || invalidRatio > 0.25 || validRatio < 0.70) {
            risk = RiskLevel.HIGH;
            statusLabel = "UNRELIABLE";
            action = "Dispatch technician: sensor is experiencing persistent outages or telemetry distortion.";
        } else if (totalOfflinePeriods > 0 || totalStalePeriods > 3 || invalidRatio > 0.10) {
            risk = RiskLevel.MEDIUM;
            statusLabel = "DEGRADING";
            action = "Inspect sensor power and transmission signal: intermittent telemetry gaps detected.";
        } else {
            risk = RiskLevel.LOW;
            statusLabel = "HEALTHY";
            action = "No maintenance required. Telemetry stream is stable and consistent.";
        }

        int reliabilityScore = (int) Math.round((validRatio * 100.0) - (totalOfflinePeriods * 8.0) - (totalStalePeriods * 2.0));
        reliabilityScore = Math.max(10, Math.min(100, reliabilityScore));

        int confidence = (int) Math.min(95, Math.max(60, 70 + (totalReadings / 20)));

        String evidence = String.format(
                "Sensor reliability is %s (Score: %d%%). Over last 14 days: %d readings (%.0f%% valid), %d stale intervals, %d offline periods.",
                statusLabel, reliabilityScore, totalReadings, validRatio * 100.0, totalStalePeriods, totalOfflinePeriods
        );

        String assumptions = String.format(
                "Evaluated across %d field sensors. Assumes steady LoRaWAN / cellular gateway reception.",
                sensorSummaries.size()
        );

        return Prediction.create(
                field.fieldId(),
                PredictionType.SENSOR_RELIABILITY_FORECAST,
                horizon,
                targetTime,
                BigDecimal.valueOf(reliabilityScore),
                BigDecimal.valueOf(Math.max(0, reliabilityScore - 10)),
                BigDecimal.valueOf(Math.min(100, reliabilityScore + 5)),
                "score",
                confidence,
                risk,
                DataSufficiency.SUFFICIENT,
                null,
                evidence,
                assumptions,
                action,
                "sensors"
        );
    }
}
