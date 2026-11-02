package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Domain service evaluating telemetry adequacy, observation frequency, freshness, and quality.
 * Enforces the strict rule: if data is insufficient, NEVER fabricate confident predictions.
 */
public class DataSufficiencyEvaluator {

    public static final int MIN_OBSERVATIONS_FOR_SUFFICIENT = 6;
    public static final int MIN_OBSERVATIONS_FOR_LIMITED = 3;
    public static final Duration MAX_FRESHNESS_SUFFICIENT = Duration.ofHours(12);
    public static final Duration MAX_FRESHNESS_LIMITED = Duration.ofHours(24);

    public record SufficiencyEvaluation(
            DataSufficiency sufficiency,
            int observationCount,
            int validReadingCount,
            int suspectReadingCount,
            int invalidReadingCount,
            Duration freshnessAge,
            int baseConfidencePercent,
            String summaryExplanation
    ) {}

    public SufficiencyEvaluation evaluate(List<TelemetryObservation> observations, Instant evaluationTime) {
        Instant now = evaluationTime != null ? evaluationTime : Instant.now();

        if (observations == null || observations.isEmpty()) {
            return new SufficiencyEvaluation(
                    DataSufficiency.INSUFFICIENT,
                    0, 0, 0, 0,
                    Duration.ofDays(365),
                    0,
                    "Zero telemetry observations found in the evaluation window."
            );
        }

        int total = observations.size();
        int valid = 0;
        int suspect = 0;
        int invalid = 0;
        Instant latestTime = Instant.EPOCH;

        for (TelemetryObservation obs : observations) {
            if (obs.measuredAt() != null && obs.measuredAt().isAfter(latestTime)) {
                latestTime = obs.measuredAt();
            }
            String quality = obs.quality() != null ? obs.quality().toUpperCase() : "VALID";
            switch (quality) {
                case "VALID" -> valid++;
                case "SUSPECT" -> suspect++;
                case "INVALID" -> invalid++;
                default -> valid++;
            }
        }

        Duration freshnessAge = Duration.between(latestTime, now);
        if (freshnessAge.isNegative()) {
            freshnessAge = Duration.ZERO;
        }

        double validRatio = total > 0 ? (double) valid / total : 0.0;

        // Check for INSUFFICIENT
        if (total < MIN_OBSERVATIONS_FOR_LIMITED || freshnessAge.compareTo(MAX_FRESHNESS_LIMITED) > 0 || validRatio < 0.50) {
            String reason = String.format(
                    "Insufficient telemetry: %d observations found (minimum %d required), freshness age %d hours (max %d hours), valid ratio %.0f%%.",
                    total, MIN_OBSERVATIONS_FOR_LIMITED, freshnessAge.toHours(), MAX_FRESHNESS_LIMITED.toHours(), validRatio * 100
            );
            return new SufficiencyEvaluation(
                    DataSufficiency.INSUFFICIENT,
                    total, valid, suspect, invalid,
                    freshnessAge,
                    0,
                    reason
            );
        }

        // Check for SUFFICIENT
        if (total >= MIN_OBSERVATIONS_FOR_SUFFICIENT && freshnessAge.compareTo(MAX_FRESHNESS_SUFFICIENT) <= 0 && validRatio >= 0.80) {
            int confidence = (int) Math.round(75 + (validRatio * 15) - Math.min(10, freshnessAge.toHours()));
            confidence = Math.min(95, Math.max(70, confidence));
            String reason = String.format(
                    "Telemetry is sufficient: %d observations (%d valid, %.0f%% valid ratio), latest telemetry %d hours ago.",
                    total, valid, validRatio * 100, freshnessAge.toHours()
            );
            return new SufficiencyEvaluation(
                    DataSufficiency.SUFFICIENT,
                    total, valid, suspect, invalid,
                    freshnessAge,
                    confidence,
                    reason
            );
        }

        // Otherwise LIMITED
        int confidence = (int) Math.round(50 + (validRatio * 15) - Math.min(15, freshnessAge.toHours()));
        confidence = Math.min(70, Math.max(40, confidence));
        String reason = String.format(
                "Telemetry is limited: %d observations (%d valid), freshness age %d hours.",
                total, valid, freshnessAge.toHours()
        );
        return new SufficiencyEvaluation(
                DataSufficiency.LIMITED,
                total, valid, suspect, invalid,
                freshnessAge,
                confidence,
                reason
        );
    }
}
