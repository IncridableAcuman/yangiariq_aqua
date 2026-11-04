package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DatasetSplit;
import com.aquaai.prediction.domain.model.FeatureRecord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Domain Service enforcing strict chronological temporal data partitioning.
 * Guaranteed anti-leakage:
 * - Train: Earliest 60% of historical timeline.
 * - Validation: Intermediate 20% of timeline.
 * - Test: Latest 20% of timeline (unseen holdout).
 */
public class TemporalDataSplitter {

    public DatasetSplit split(List<FeatureRecord> records) {
        if (records == null || records.isEmpty()) {
            Instant now = Instant.now();
            return new DatasetSplit(List.of(), List.of(), List.of(), "TEMPORAL_60_20_20", now, now, now, now, now, now);
        }

        // 1. Sort strictly chronologically by observation timestamp
        List<FeatureRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(FeatureRecord::timestamp));

        int n = sorted.size();
        int trainEndIdx = (int) Math.round(n * 0.60);
        int valEndIdx = (int) Math.round(n * 0.80);

        // Ensure at least 1 sample in each bucket if sample size allows
        if (trainEndIdx < 1) trainEndIdx = 1;
        if (valEndIdx <= trainEndIdx && trainEndIdx < n) valEndIdx = trainEndIdx + 1;
        if (valEndIdx > n) valEndIdx = n;

        List<FeatureRecord> train = sorted.subList(0, trainEndIdx);
        List<FeatureRecord> val = sorted.subList(trainEndIdx, valEndIdx);
        List<FeatureRecord> test = sorted.subList(valEndIdx, n);

        Instant trainStart = train.get(0).timestamp();
        Instant trainEnd = train.get(train.size() - 1).timestamp();

        Instant valStart = !val.isEmpty() ? val.get(0).timestamp() : trainEnd;
        Instant valEnd = !val.isEmpty() ? val.get(val.size() - 1).timestamp() : valStart;

        Instant testStart = !test.isEmpty() ? test.get(0).timestamp() : valEnd;
        Instant testEnd = !test.isEmpty() ? test.get(test.size() - 1).timestamp() : testStart;

        return new DatasetSplit(
                train,
                val,
                test,
                "TEMPORAL_60_20_20",
                trainStart,
                trainEnd,
                valStart,
                valEnd,
                testStart,
                testEnd
        );
    }
}
