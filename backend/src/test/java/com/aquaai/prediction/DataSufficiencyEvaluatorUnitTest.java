package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.port.PredictionDataProvider.TelemetryObservation;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DataSufficiencyEvaluatorUnitTest {

    private DataSufficiencyEvaluator evaluator;
    private Instant now;

    @BeforeEach
    void setUp() {
        evaluator = new DataSufficiencyEvaluator();
        now = Instant.now();
    }

    @Test
    @DisplayName("Empty observations result in INSUFFICIENT sufficiency and 0 confidence")
    void emptyObservations() {
        SufficiencyEvaluation result = evaluator.evaluate(Collections.emptyList(), now);
        assertThat(result.sufficiency()).isEqualTo(DataSufficiency.INSUFFICIENT);
        assertThat(result.baseConfidencePercent()).isZero();
    }

    @Test
    @DisplayName("Fewer than 3 observations result in INSUFFICIENT")
    void fewerThanThreeObservations() {
        List<TelemetryObservation> obs = List.of(
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(30.0), "VALID", now.minus(Duration.ofHours(2))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(29.5), "VALID", now.minus(Duration.ofHours(1)))
        );
        SufficiencyEvaluation result = evaluator.evaluate(obs, now);
        assertThat(result.sufficiency()).isEqualTo(DataSufficiency.INSUFFICIENT);
        assertThat(result.baseConfidencePercent()).isZero();
    }

    @Test
    @DisplayName("Stale observations (> 24 hours old) result in INSUFFICIENT")
    void staleObservations() {
        List<TelemetryObservation> obs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            obs.add(new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(28.0), "VALID", now.minus(Duration.ofHours(30 + i))));
        }
        SufficiencyEvaluation result = evaluator.evaluate(obs, now);
        assertThat(result.sufficiency()).isEqualTo(DataSufficiency.INSUFFICIENT);
        assertThat(result.freshnessAge()).isGreaterThan(Duration.ofHours(24));
    }

    @Test
    @DisplayName("3-5 observations within 24 hours result in LIMITED sufficiency")
    void limitedObservations() {
        List<TelemetryObservation> obs = List.of(
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(32.0), "VALID", now.minus(Duration.ofHours(6))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(31.5), "VALID", now.minus(Duration.ofHours(4))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(31.0), "VALID", now.minus(Duration.ofHours(2))),
                new TelemetryObservation(UUID.randomUUID(), "SOIL_MOISTURE", BigDecimal.valueOf(30.8), "VALID", now.minus(Duration.ofHours(1)))
        );
        SufficiencyEvaluation result = evaluator.evaluate(obs, now);
        assertThat(result.sufficiency()).isEqualTo(DataSufficiency.LIMITED);
        assertThat(result.baseConfidencePercent()).isBetween(40, 70);
    }

    @Test
    @DisplayName(">=6 valid observations within 12 hours result in SUFFICIENT sufficiency with high confidence")
    void sufficientObservations() {
        List<TelemetryObservation> obs = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            obs.add(new TelemetryObservation(
                    UUID.randomUUID(),
                    "SOIL_MOISTURE",
                    BigDecimal.valueOf(30.0 - (i * 0.2)),
                    "VALID",
                    now.minus(Duration.ofHours(8 - i))
            ));
        }
        SufficiencyEvaluation result = evaluator.evaluate(obs, now);
        assertThat(result.sufficiency()).isEqualTo(DataSufficiency.SUFFICIENT);
        assertThat(result.baseConfidencePercent()).isGreaterThanOrEqualTo(75);
    }
}
