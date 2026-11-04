package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DatasetSplit;
import com.aquaai.prediction.domain.model.FeatureRecord;
import com.aquaai.prediction.domain.service.FeatureExtractor;
import com.aquaai.prediction.domain.service.RidgeRegressionTrainer;
import com.aquaai.prediction.domain.service.RidgeRegressionTrainer.TrainedModelResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RidgeRegressionTrainerUnitTest {

    private final RidgeRegressionTrainer trainer = new RidgeRegressionTrainer(1.0);

    @Test
    @DisplayName("Trains JVM-native Ridge regression model deterministically, computing reproducible artifact hash and metrics")
    void trainsModelDeterministically() {
        UUID fieldId = UUID.randomUUID();
        Instant baseTime = Instant.parse("2026-06-01T00:00:00Z");

        List<FeatureRecord> trainRecords = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            double[] x = new double[FeatureExtractor.FEATURE_COUNT];
            x[0] = 20.0 + (i % 10); // current moisture
            x[1] = -0.15;           // slope
            x[2] = 30.0;            // temp
            x[3] = 40.0;            // humidity
            x[4] = 5.0;             // et0
            x[6] = 1.0;             // cotton
            x[7] = 3.0;             // loam
            x[8] = 1.0;             // drip
            x[9] = 10.0;            // area
            x[11] = 75.0;           // headroom
            x[12] = 2.0;            // summer
            x[13] = 1.0;            // quality score

            double target = x[0] - 2.5; // ground truth: current moisture minus 2.5%
            double baseline = x[0] - 3.6; // baseline slightly over-depleting

            trainRecords.add(new FeatureRecord(
                    UUID.randomUUID(), fieldId, baseTime.plusSeconds(i * 3600),
                    x, target, baseline
            ));
        }

        List<FeatureRecord> valRecords = new ArrayList<>();
        for (int i = 60; i < 80; i++) {
            double[] x = new double[FeatureExtractor.FEATURE_COUNT];
            x[0] = 22.0; x[1] = -0.15; x[2] = 30.0; x[3] = 40.0; x[4] = 5.0;
            x[6] = 1.0; x[7] = 3.0; x[8] = 1.0; x[9] = 10.0; x[11] = 75.0; x[12] = 2.0; x[13] = 1.0;
            valRecords.add(new FeatureRecord(
                    UUID.randomUUID(), fieldId, baseTime.plusSeconds(i * 3600),
                    x, x[0] - 2.5, x[0] - 3.6
            ));
        }

        List<FeatureRecord> testRecords = new ArrayList<>();
        for (int i = 80; i < 100; i++) {
            double[] x = new double[FeatureExtractor.FEATURE_COUNT];
            x[0] = 25.0; x[1] = -0.15; x[2] = 30.0; x[3] = 40.0; x[4] = 5.0;
            x[6] = 1.0; x[7] = 3.0; x[8] = 1.0; x[9] = 10.0; x[11] = 75.0; x[12] = 2.0; x[13] = 1.0;
            testRecords.add(new FeatureRecord(
                    UUID.randomUUID(), fieldId, baseTime.plusSeconds(i * 3600),
                    x, x[0] - 2.5, x[0] - 3.6
            ));
        }

        DatasetSplit split = new DatasetSplit(
                trainRecords, valRecords, testRecords, "TEMPORAL_60_20_20",
                baseTime, baseTime.plusSeconds(59 * 3600),
                baseTime.plusSeconds(60 * 3600), baseTime.plusSeconds(79 * 3600),
                baseTime.plusSeconds(80 * 3600), baseTime.plusSeconds(99 * 3600)
        );

        TrainedModelResult result1 = trainer.train(split);
        TrainedModelResult result2 = trainer.train(split);

        // Determinism: artifact hashes must match identically
        assertThat(result1.artifactHash()).isEqualTo(result2.artifactHash());
        assertThat(result1.weights()).containsExactly(result2.weights());

        // Performance: test MAE should be low
        assertThat(result1.testMetrics().mae()).isLessThan(result1.baselineTestMetrics().mae());
        assertThat(result1.baselineComparison().maeImprovementPercent()).isGreaterThan(0.0);
        assertThat(result1.baselineComparison().regressionDetected()).isFalse();
        assertThat(result1.configurationJson()).contains("\"modelAlgorithm\":\"RIDGE_REGRESSION\"");
    }
}
