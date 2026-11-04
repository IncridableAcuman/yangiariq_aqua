package com.aquaai.prediction.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Explicit dataset quality metrics evaluated before candidate model training.
 * Enforces quality gates: invalid, corrupted, or outlier observations never silently enter training.
 */
public record TrainingDatasetQuality(
        int totalRecords,
        int validRecords,
        int rejectedRecords,
        int missingFeatureCount,
        int missingTargetCount,
        int duplicateCount,
        int outlierCount,
        DataQualityStatus qualityStatus,
        List<String> rejectionReasons
) {
    public enum DataQualityStatus {
        HIGH_QUALITY,
        ACCEPTABLE,
        DEGRADED,
        INSUFFICIENT
    }

    public TrainingDatasetQuality {
        rejectionReasons = rejectionReasons != null ? List.copyOf(rejectionReasons) : List.of();
        qualityStatus = Objects.requireNonNullElse(qualityStatus, DataQualityStatus.INSUFFICIENT);
    }

    public boolean isSufficientForTraining(int minRequiredSamples) {
        return qualityStatus != DataQualityStatus.INSUFFICIENT && validRecords >= minRequiredSamples;
    }

    public static TrainingDatasetQuality empty(String reason) {
        return new TrainingDatasetQuality(
                0, 0, 0, 0, 0, 0, 0,
                DataQualityStatus.INSUFFICIENT,
                List.of(reason)
        );
    }
}
