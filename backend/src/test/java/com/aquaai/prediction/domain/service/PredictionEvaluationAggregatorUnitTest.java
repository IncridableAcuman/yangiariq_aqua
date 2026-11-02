package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.EvaluationConfidenceTier;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationDataQualityImpact;
import com.aquaai.prediction.domain.model.EvaluationDriftSummary;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionClassificationResult;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionEvaluationAggregatorUnitTest {

    private PredictionEvaluationAggregator aggregator;
    private UUID fieldId;
    private Instant now;

    @BeforeEach
    void setUp() {
        aggregator = new PredictionEvaluationAggregator();
        fieldId = UUID.randomUUID();
        now = Instant.now();
    }

    private PredictionEvaluation createNumericalEval(BigDecimal predicted, BigDecimal actual, int confidence, EvaluationDataQuality quality) {
        return PredictionEvaluation.createNumerical(
                UUID.randomUUID(),
                fieldId,
                PredictionType.SOIL_MOISTURE_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                "DETERMINISTIC_V1",
                now.minusSeconds(86400),
                now,
                now,
                predicted,
                predicted.subtract(BigDecimal.valueOf(2)),
                predicted.add(BigDecimal.valueOf(2)),
                actual,
                null,
                null,
                null,
                null,
                false,
                confidence,
                EvaluationStatus.EVALUATED,
                null,
                quality,
                "TEST",
                "test"
        );
    }

    private PredictionEvaluation createClassificationEval(PredictionClassificationResult result, int confidence) {
        return PredictionEvaluation.createCategorical(
                UUID.randomUUID(),
                fieldId,
                PredictionType.IRRIGATION_NEED_FORECAST,
                PredictionHorizon.NEXT_24_HOURS,
                "DETERMINISTIC_V1",
                now.minusSeconds(86400),
                now,
                now,
                "LIKELY",
                "IRRIGATED",
                result,
                null,
                null,
                null,
                null,
                false,
                confidence,
                EvaluationStatus.EVALUATED,
                null,
                EvaluationDataQuality.DATA_COMPLETE,
                "TEST",
                "test"
        );
    }

    @Test
    @DisplayName("Sample size below minimum policy returns null metrics without fabricating performance")
    void sampleSizeBelowPolicyReturnsNullMetrics() {
        List<PredictionEvaluation> evals = List.of(
                createNumericalEval(new BigDecimal("25.0"), new BigDecimal("27.0"), 80, EvaluationDataQuality.DATA_COMPLETE),
                createNumericalEval(new BigDecimal("30.0"), new BigDecimal("29.0"), 85, EvaluationDataQuality.DATA_COMPLETE)
        );

        NumericalEvaluationMetrics metrics = aggregator.calculateNumericalMetrics(evals, 10);

        assertThat(metrics.sampleSize()).isEqualTo(2);
        assertThat(metrics.isSufficientSample()).isFalse();
        assertThat(metrics.meanAbsoluteError()).isNull();
        assertThat(metrics.rootMeanSquaredError()).isNull();
        assertThat(metrics.meanBiasError()).isNull();
    }

    @Test
    @DisplayName("Sufficient numerical sample calculates exact MAE, RMSE, and MBE")
    void sufficientNumericalSampleCalculatesExactMetrics() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        // 10 predictions: predicted = 25, actual = 20 -> diff = 5, dir = +5
        for (int i = 0; i < 10; i++) {
            evals.add(createNumericalEval(new BigDecimal("25.0"), new BigDecimal("20.0"), 80, EvaluationDataQuality.DATA_COMPLETE));
        }

        NumericalEvaluationMetrics metrics = aggregator.calculateNumericalMetrics(evals, 10);

        assertThat(metrics.isSufficientSample()).isTrue();
        assertThat(metrics.sampleSize()).isEqualTo(10);
        assertThat(metrics.meanAbsoluteError()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(metrics.rootMeanSquaredError()).isEqualByComparingTo(new BigDecimal("5.00"));
        assertThat(metrics.meanBiasError()).isEqualByComparingTo(new BigDecimal("5.00")); // systematic overprediction
        assertThat(metrics.isMapeApplicable()).isTrue();
        assertThat(metrics.meanAbsolutePercentageError()).isEqualByComparingTo(new BigDecimal("25.00")); // (5/20) * 100
    }

    @Test
    @DisplayName("Safe MAPE: When any actual value is zero, MAPE is flagged not applicable")
    void safeMapeHandlesZeroActual() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            evals.add(createNumericalEval(new BigDecimal("10.0"), new BigDecimal("12.0"), 80, EvaluationDataQuality.DATA_COMPLETE));
        }
        // Add one evaluation with actual = 0.0
        evals.add(createNumericalEval(new BigDecimal("5.0"), BigDecimal.ZERO, 80, EvaluationDataQuality.DATA_COMPLETE));

        NumericalEvaluationMetrics metrics = aggregator.calculateNumericalMetrics(evals, 10);

        assertThat(metrics.isSufficientSample()).isTrue();
        assertThat(metrics.isMapeApplicable()).isFalse();
        assertThat(metrics.meanAbsolutePercentageError()).isNull();
    }

    @Test
    @DisplayName("Classification metrics calculate Accuracy, Precision, Recall, and F1 with minimum sample gating")
    void classificationMetricsCalculation() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        // 6 True Positives, 2 True Negatives, 1 False Positive, 1 False Negative = 10 total
        for (int i = 0; i < 6; i++) evals.add(createClassificationEval(PredictionClassificationResult.TRUE_POSITIVE, 80));
        for (int i = 0; i < 2; i++) evals.add(createClassificationEval(PredictionClassificationResult.TRUE_NEGATIVE, 80));
        evals.add(createClassificationEval(PredictionClassificationResult.FALSE_POSITIVE, 80));
        evals.add(createClassificationEval(PredictionClassificationResult.FALSE_NEGATIVE, 80));

        ClassificationEvaluationMetrics metrics = aggregator.calculateClassificationMetrics(evals, 10);

        assertThat(metrics.isSufficientSample()).isTrue();
        assertThat(metrics.sampleSize()).isEqualTo(10);
        assertThat(metrics.truePositives()).isEqualTo(6);
        assertThat(metrics.trueNegatives()).isEqualTo(2);
        assertThat(metrics.falsePositives()).isEqualTo(1);
        assertThat(metrics.falseNegatives()).isEqualTo(1);

        // Accuracy: (6 + 2) / 10 = 80.00%
        assertThat(metrics.accuracyPercent()).isEqualByComparingTo(new BigDecimal("80.00"));
        // Precision: 6 / (6 + 1) = 85.71%
        assertThat(metrics.precisionPercent()).isEqualByComparingTo(new BigDecimal("85.71"));
        // Recall: 6 / (6 + 1) = 85.71%
        assertThat(metrics.recallPercent()).isEqualByComparingTo(new BigDecimal("85.71"));
        // F1: 85.71%
        assertThat(metrics.f1ScorePercent()).isEqualByComparingTo(new BigDecimal("85.71"));
    }

    @Test
    @DisplayName("Confidence tiers evaluate performance by confidence range")
    void confidenceTiersEvaluation() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        // 6 high-confidence items (85%)
        for (int i = 0; i < 6; i++) {
            evals.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("21.0"), 85, EvaluationDataQuality.DATA_COMPLETE));
        }
        // 6 medium-confidence items (60%)
        for (int i = 0; i < 6; i++) {
            evals.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("24.0"), 60, EvaluationDataQuality.DATA_PARTIAL));
        }

        List<EvaluationConfidenceTier> tiers = aggregator.calculateConfidenceTiers(evals);

        assertThat(tiers).hasSize(3);
        EvaluationConfidenceTier highTier = tiers.stream().filter(t -> "HIGH".equals(t.tierName())).findFirst().orElseThrow();
        EvaluationConfidenceTier medTier = tiers.stream().filter(t -> "MEDIUM".equals(t.tierName())).findFirst().orElseThrow();

        assertThat(highTier.sampleSize()).isEqualTo(6);
        assertThat(highTier.isSufficientSample()).isTrue();
        assertThat(highTier.meanAbsoluteError()).isEqualByComparingTo(new BigDecimal("1.00"));

        assertThat(medTier.sampleSize()).isEqualTo(6);
        assertThat(medTier.isSufficientSample()).isTrue();
        assertThat(medTier.meanAbsoluteError()).isEqualByComparingTo(new BigDecimal("4.00"));
    }

    @Test
    @DisplayName("Data quality breakdown groups accuracy and error by telemetry condition")
    void dataQualityBreakdown() {
        List<PredictionEvaluation> evals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            evals.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("21.0"), 85, EvaluationDataQuality.DATA_COMPLETE));
        }
        for (int i = 0; i < 5; i++) {
            evals.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("25.0"), 60, EvaluationDataQuality.DATA_PARTIAL));
        }

        List<EvaluationDataQualityImpact> qualityImpact = aggregator.calculateDataQualityImpact(evals);

        assertThat(qualityImpact).hasSize(3);
        EvaluationDataQualityImpact complete = qualityImpact.stream().filter(q -> q.dataQuality() == EvaluationDataQuality.DATA_COMPLETE).findFirst().orElseThrow();
        EvaluationDataQualityImpact partial = qualityImpact.stream().filter(q -> q.dataQuality() == EvaluationDataQuality.DATA_PARTIAL).findFirst().orElseThrow();

        assertThat(complete.sampleSize()).isEqualTo(5);
        assertThat(complete.meanAbsoluteError()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(partial.meanAbsoluteError()).isEqualByComparingTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("Drift detection flags degradation when error increases by over 25%")
    void driftDetectionFlagsDegradation() {
        List<PredictionEvaluation> current = new ArrayList<>();
        List<PredictionEvaluation> previous = new ArrayList<>();

        // Previous: MAE = 2.00
        for (int i = 0; i < 10; i++) {
            previous.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("22.0"), 80, EvaluationDataQuality.DATA_COMPLETE));
        }
        // Current: MAE = 4.00 (+100% increase)
        for (int i = 0; i < 10; i++) {
            current.add(createNumericalEval(new BigDecimal("20.0"), new BigDecimal("24.0"), 80, EvaluationDataQuality.DATA_COMPLETE));
        }

        EvaluationDriftSummary drift = aggregator.calculateDrift(current, previous, 10);

        assertThat(drift.driftDetected()).isTrue();
        assertThat(drift.status()).isEqualTo(ModelHealthStatus.DEGRADED);
        assertThat(drift.percentageChange()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
