package com.aquaai.prediction.domain.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * Normalized observation record with constructed features and verified ground-truth target.
 */
public record FeatureRecord(
        UUID observationId,
        UUID fieldId,
        Instant timestamp,
        double[] features,
        double targetValue,
        double baselinePredictedValue
) {
    public FeatureRecord {
        features = features != null ? Arrays.copyOf(features, features.length) : new double[0];
    }

    public double[] copyFeatures() {
        return Arrays.copyOf(features, features.length);
    }
}
