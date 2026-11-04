package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.TrainingDatasetQuality;
import com.aquaai.prediction.domain.service.DatasetQualityGate;
import com.aquaai.prediction.domain.service.DatasetQualityGate.QualityGateResult;
import com.aquaai.prediction.domain.service.DatasetQualityGate.RawObservationSample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DatasetQualityGateUnitTest {

    private final DatasetQualityGate qualityGate = new DatasetQualityGate();

    @Test
    @DisplayName("Quality gate rejects empty or null samples with INSUFFICIENT status")
    void rejectsEmptySamples() {
        QualityGateResult result = qualityGate.filterAndValidate(List.of(), 30);
        assertThat(result.quality().qualityStatus()).isEqualTo(TrainingDatasetQuality.DataQualityStatus.INSUFFICIENT);
        assertThat(result.validRecords()).isEmpty();
        assertThat(result.quality().isSufficientForTraining(30)).isFalse();
    }

    @Test
    @DisplayName("Quality gate filters INVALID telemetry, duplicates, outliers, and missing targets")
    void filtersCorruptedAndOutlierObservations() {
        UUID fieldId = UUID.randomUUID();
        Instant baseTime = Instant.parse("2026-06-01T10:00:00Z");

        List<RawObservationSample> samples = new ArrayList<>();

        // 35 valid samples
        for (int i = 0; i < 35; i++) {
            samples.add(new RawObservationSample(
                    UUID.randomUUID(), fieldId, baseTime.plusSeconds(i * 3600),
                    new double[]{25.0 + (i % 5), -0.1}, 24.0, 24.5, "VALID"
            ));
        }

        // Duplicate
        samples.add(new RawObservationSample(
                UUID.randomUUID(), fieldId, baseTime, // duplicate timestamp for fieldId
                new double[]{25.0, -0.1}, 24.0, 24.5, "VALID"
        ));

        // Invalid telemetry
        samples.add(new RawObservationSample(
                UUID.randomUUID(), fieldId, baseTime.plusSeconds(36 * 3600),
                new double[]{25.0, -0.1}, 24.0, 24.5, "INVALID"
        ));

        // Missing target
        samples.add(new RawObservationSample(
                UUID.randomUUID(), fieldId, baseTime.plusSeconds(37 * 3600),
                new double[]{25.0, -0.1}, null, 24.5, "VALID"
        ));

        // Outlier target (> 60%)
        samples.add(new RawObservationSample(
                UUID.randomUUID(), fieldId, baseTime.plusSeconds(38 * 3600),
                new double[]{25.0, -0.1}, 85.0, 24.5, "VALID"
        ));

        // Outlier target (< 5%)
        samples.add(new RawObservationSample(
                UUID.randomUUID(), fieldId, baseTime.plusSeconds(39 * 3600),
                new double[]{25.0, -0.1}, 2.0, 24.5, "VALID"
        ));

        QualityGateResult result = qualityGate.filterAndValidate(samples, 30);

        assertThat(result.validRecords()).hasSize(35);
        assertThat(result.quality().totalRecords()).isEqualTo(40);
        assertThat(result.quality().rejectedRecords()).isEqualTo(5);
        assertThat(result.quality().duplicateCount()).isEqualTo(1);
        assertThat(result.quality().missingTargetCount()).isEqualTo(1);
        assertThat(result.quality().outlierCount()).isEqualTo(3); // 1 INVALID + 2 target outliers
        assertThat(result.quality().qualityStatus()).isEqualTo(TrainingDatasetQuality.DataQualityStatus.HIGH_QUALITY);
        assertThat(result.quality().isSufficientForTraining(30)).isTrue();
    }

    @Test
    @DisplayName("Quality gate flags INSUFFICIENT if valid count is below minimum gate (n < 30)")
    void flagsInsufficientWhenSampleCountBelowThreshold() {
        UUID fieldId = UUID.randomUUID();
        Instant baseTime = Instant.parse("2026-06-01T10:00:00Z");

        List<RawObservationSample> samples = new ArrayList<>();
        // Only 15 valid samples
        for (int i = 0; i < 15; i++) {
            samples.add(new RawObservationSample(
                    UUID.randomUUID(), fieldId, baseTime.plusSeconds(i * 3600),
                    new double[]{25.0, -0.1}, 24.0, 24.5, "VALID"
            ));
        }

        QualityGateResult result = qualityGate.filterAndValidate(samples, 30);

        assertThat(result.validRecords()).hasSize(15);
        assertThat(result.quality().qualityStatus()).isEqualTo(TrainingDatasetQuality.DataQualityStatus.INSUFFICIENT);
        assertThat(result.quality().isSufficientForTraining(30)).isFalse();
        assertThat(result.quality().rejectionReasons()).anyMatch(r -> r.contains("below minimum"));
    }
}
