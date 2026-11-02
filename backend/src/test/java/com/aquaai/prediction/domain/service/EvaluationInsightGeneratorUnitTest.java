package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.EvaluationConfidenceTier;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationInsight;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EvaluationInsightGeneratorUnitTest {

    private EvaluationInsightGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new EvaluationInsightGenerator();
    }

    @Test
    @DisplayName("Generates bias insight when persistent directional error exceeds threshold")
    void generatesBiasInsight() {
        NumericalEvaluationMetrics num = new NumericalEvaluationMetrics(
                15, 10, true, new BigDecimal("12.00"), new BigDecimal("14.00"),
                new BigDecimal("20.00"), true, new BigDecimal("+9.50"), 10, new BigDecimal("66.67"));

        List<PredictionEvaluation> evals = new ArrayList<>();
        List<EvaluationInsight> insights = generator.generateInsights(evals, num, null, null);

        assertThat(insights).anyMatch(i -> "BIAS".equals(i.category()) && i.title().contains("Over-Prediction"));
    }

    @Test
    @DisplayName("Generates safety insight when operations were halted by hydraulic safety interlock")
    void generatesSafetyInsight() {
        PredictionEvaluation eval = PredictionEvaluation.builder()
                .id(UUID.randomUUID())
                .predictionId(UUID.randomUUID())
                .fieldId(UUID.randomUUID())
                .predictionType(PredictionType.IRRIGATION_NEED_FORECAST)
                .horizon(PredictionHorizon.NEXT_24_HOURS)
                .modelVersion("DETERMINISTIC_V1")
                .generatedAt(Instant.now())
                .targetTime(Instant.now())
                .evaluatedAt(Instant.now())
                .confidencePercent(85)
                .evaluationStatus(EvaluationStatus.OUTCOME_INTERRUPTED)
                .failureReason(EvaluationFailureReason.SAFETY_EVENT)
                .safetyInterlockTriggered(true)
                .dataQuality(EvaluationDataQuality.DATA_COMPLETE)
                .evaluationMethod("SAFETY_AUDIT")
                .notes("Halted")
                .build();

        List<EvaluationInsight> insights = generator.generateInsights(List.of(eval), null, null, null);

        assertThat(insights).anyMatch(i -> "SAFETY".equals(i.category()) && i.title().contains("Safety"));
    }

    @Test
    @DisplayName("Generates telemetry bottleneck insight when evaluations fail due to missing sensors")
    void generatesTelemetryBottleneckInsight() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            evals.add(PredictionEvaluation.builder()
                    .id(UUID.randomUUID())
                    .predictionId(UUID.randomUUID())
                    .fieldId(UUID.randomUUID())
                    .predictionType(PredictionType.SOIL_MOISTURE_FORECAST)
                    .horizon(PredictionHorizon.NEXT_24_HOURS)
                    .modelVersion("DETERMINISTIC_V1")
                    .generatedAt(Instant.now())
                    .targetTime(Instant.now())
                    .evaluatedAt(Instant.now())
                    .confidencePercent(70)
                    .evaluationStatus(EvaluationStatus.INSUFFICIENT_DATA)
                    .failureReason(EvaluationFailureReason.MISSING_SENSOR)
                    .dataQuality(EvaluationDataQuality.DATA_INSUFFICIENT)
                    .evaluationMethod("AUDIT")
                    .build());
        }

        List<EvaluationInsight> insights = generator.generateInsights(evals, null, null, null);

        assertThat(insights).anyMatch(i -> "TELEMETRY".equals(i.category()) && i.title().contains("Sensor"));
    }
}
