package com.aquaai.prediction.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Strictly temporal chronological partition of feature records.
 * Prevents temporal leakage: Train (earliest 60%) -> Validation (middle 20%) -> Test (latest 20%).
 */
public record DatasetSplit(
        List<FeatureRecord> trainRecords,
        List<FeatureRecord> validationRecords,
        List<FeatureRecord> testRecords,
        String splitStrategy,
        Instant trainPeriodStart,
        Instant trainPeriodEnd,
        Instant validationPeriodStart,
        Instant validationPeriodEnd,
        Instant testPeriodStart,
        Instant testPeriodEnd
) {
    public DatasetSplit {
        trainRecords = trainRecords != null ? List.copyOf(trainRecords) : List.of();
        validationRecords = validationRecords != null ? List.copyOf(validationRecords) : List.of();
        testRecords = testRecords != null ? List.copyOf(testRecords) : List.of();
    }

    public int totalSamples() {
        return trainRecords.size() + validationRecords.size() + testRecords.size();
    }
}
