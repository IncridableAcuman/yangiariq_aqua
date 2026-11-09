package com.aquaai.pilot.domain;

import com.aquaai.pilot.domain.model.PredictionEvaluationStatus;
import com.aquaai.pilot.domain.model.PredictionValidationRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PredictionValidationUnitTest {

    @Test
    @DisplayName("Should accurately calculate absolute and relative percentage errors")
    void shouldCalculateErrorsAccurately() {
        UUID pilotId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        UUID gtId = UUID.randomUUID();

        PredictionValidationRecord record = PredictionValidationRecord.create(
                pilotId,
                fieldId,
                "PRED-SOIL-101",
                "HybridSoilMoistureModel",
                "v2.1.0",
                Instant.now().minusSeconds(3600 * 24),
                24,
                45.0, // predicted 45.0%
                50.0, // actual was 50.0%
                gtId
        );

        assertThat(record.getAbsoluteError()).isEqualTo(5.0);
        assertThat(record.getRelativeErrorPercentage()).isCloseTo(10.0, within(0.001));
        assertThat(record.getEvaluationStatus()).isEqualTo(PredictionEvaluationStatus.VALIDATED);
    }
}
