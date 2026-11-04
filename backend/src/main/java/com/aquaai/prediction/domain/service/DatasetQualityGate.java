package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.FeatureRecord;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality;
import com.aquaai.prediction.domain.model.TrainingDatasetQuality.DataQualityStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Domain Service enforcing strict data quality gates prior to model training.
 * Corrupted, invalid, duplicate, or outlier observations are rejected with explicit audit tracking.
 */
public class DatasetQualityGate {

    public record QualityGateResult(
            TrainingDatasetQuality quality,
            List<FeatureRecord> validRecords
    ) {}

    public QualityGateResult filterAndValidate(
            List<RawObservationSample> rawSamples,
            int minRequiredSamples
    ) {
        if (rawSamples == null || rawSamples.isEmpty()) {
            TrainingDatasetQuality quality = new TrainingDatasetQuality(
                    0, 0, 0, 0, 0, 0, 0,
                    DataQualityStatus.INSUFFICIENT,
                    List.of("No historical records available in the selected window.")
            );
            return new QualityGateResult(quality, List.of());
        }

        int total = rawSamples.size();
        int missingFeatures = 0;
        int missingTarget = 0;
        int duplicates = 0;
        int outliers = 0;
        List<String> rejectionReasons = new ArrayList<>();
        List<FeatureRecord> validRecords = new ArrayList<>();

        Set<String> seenObservations = new HashSet<>();

        for (RawObservationSample s : rawSamples) {
            // 1. Required fields & null check
            if (s.fieldId == null || s.timestamp == null || s.features == null) {
                missingFeatures++;
                continue;
            }

            // 2. Duplicate detection (Field + Timestamp key)
            String dedupKey = s.fieldId.toString() + "_" + s.timestamp.getEpochSecond();
            if (!seenObservations.add(dedupKey)) {
                duplicates++;
                continue;
            }

            // 3. Telemetry quality gate
            if ("INVALID".equalsIgnoreCase(s.telemetryQuality)) {
                outliers++;
                continue;
            }

            // 4. Target availability check
            if (s.targetValue == null || Double.isNaN(s.targetValue)) {
                missingTarget++;
                continue;
            }

            // 5. Outlier rejection: soil moisture must be physically within [5.0%, 60.0%]
            if (s.targetValue < 5.0 || s.targetValue > 60.0) {
                outliers++;
                continue;
            }

            // Check current moisture feature (index 0) if present
            if (s.features.length > 0 && (s.features[0] < 5.0 || s.features[0] > 60.0)) {
                outliers++;
                continue;
            }

            // Passed all gates
            validRecords.add(new FeatureRecord(
                    s.observationId != null ? s.observationId : UUID.randomUUID(),
                    s.fieldId,
                    s.timestamp,
                    s.features,
                    s.targetValue,
                    s.baselinePredictedValue != null ? s.baselinePredictedValue : s.features[0]
            ));
        }

        int rejected = total - validRecords.size();
        if (missingFeatures > 0) rejectionReasons.add(missingFeatures + " records discarded due to missing required feature attributes.");
        if (missingTarget > 0) rejectionReasons.add(missingTarget + " records discarded due to missing ground-truth target outcomes.");
        if (duplicates > 0) rejectionReasons.add(duplicates + " duplicate telemetry observation records removed.");
        if (outliers > 0) rejectionReasons.add(outliers + " records rejected due to out-of-range sensor readings or INVALID telemetry quality.");

        DataQualityStatus status;
        if (validRecords.size() < minRequiredSamples) {
            status = DataQualityStatus.INSUFFICIENT;
            rejectionReasons.add("Valid sample count (" + validRecords.size() + ") is below minimum required sample threshold (" + minRequiredSamples + ").");
        } else {
            double validRatio = (double) validRecords.size() / total;
            if (validRatio >= 0.80) {
                status = DataQualityStatus.HIGH_QUALITY;
            } else if (validRatio >= 0.60) {
                status = DataQualityStatus.ACCEPTABLE;
            } else if (validRatio >= 0.40) {
                status = DataQualityStatus.DEGRADED;
            } else {
                status = DataQualityStatus.INSUFFICIENT;
            }
        }

        TrainingDatasetQuality quality = new TrainingDatasetQuality(
                total,
                validRecords.size(),
                rejected,
                missingFeatures,
                missingTarget,
                duplicates,
                outliers,
                status,
                rejectionReasons
        );

        return new QualityGateResult(quality, validRecords);
    }

    public static class RawObservationSample {
        public UUID observationId;
        public UUID fieldId;
        public java.time.Instant timestamp;
        public double[] features;
        public Double targetValue;
        public Double baselinePredictedValue;
        public String telemetryQuality;

        public RawObservationSample(
                UUID observationId,
                UUID fieldId,
                java.time.Instant timestamp,
                double[] features,
                Double targetValue,
                Double baselinePredictedValue,
                String telemetryQuality
        ) {
            this.observationId = observationId;
            this.fieldId = fieldId;
            this.timestamp = timestamp;
            this.features = features;
            this.targetValue = targetValue;
            this.baselinePredictedValue = baselinePredictedValue;
            this.telemetryQuality = telemetryQuality;
        }
    }
}
