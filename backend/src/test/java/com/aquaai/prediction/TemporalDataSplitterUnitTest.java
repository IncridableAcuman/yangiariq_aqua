package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DatasetSplit;
import com.aquaai.prediction.domain.model.FeatureRecord;
import com.aquaai.prediction.domain.service.TemporalDataSplitter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalDataSplitterUnitTest {

    private final TemporalDataSplitter splitter = new TemporalDataSplitter();

    @Test
    @DisplayName("Splits records strictly chronologically into 60% Train, 20% Val, 20% Test with non-overlapping windows")
    void splitsChronologicallyWithoutLeakage() {
        UUID fieldId = UUID.randomUUID();
        Instant baseTime = Instant.parse("2026-05-01T00:00:00Z");

        // 100 historical records ordered randomly in list to test sorting
        List<FeatureRecord> records = new ArrayList<>();
        for (int i = 99; i >= 0; i--) { // Reverse insertion
            records.add(new FeatureRecord(
                    UUID.randomUUID(), fieldId,
                    baseTime.plusSeconds(i * 3600),
                    new double[]{25.0, -0.1},
                    24.0, 24.5
            ));
        }

        DatasetSplit split = splitter.split(records);

        assertThat(split.trainRecords()).hasSize(60);
        assertThat(split.validationRecords()).hasSize(20);
        assertThat(split.testRecords()).hasSize(20);

        // Anti-leakage: Train end <= Val start <= Val end <= Test start
        assertThat(split.trainPeriodEnd()).isBeforeOrEqualTo(split.validationPeriodStart());
        assertThat(split.validationPeriodEnd()).isBeforeOrEqualTo(split.testPeriodStart());

        // Test records must be the latest chronologically
        assertThat(split.testPeriodEnd()).isEqualTo(baseTime.plusSeconds(99 * 3600));
        assertThat(split.trainPeriodStart()).isEqualTo(baseTime);
    }
}
