package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.EvaluationConfidenceTier;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationFailureReason;
import com.aquaai.prediction.domain.model.EvaluationInsight;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain Service that generates actionable, audit-backed evaluation insights.
 * <p>
 * Insights are strictly deterministic and grounded in evaluated prediction records.
 */
public class EvaluationInsightGenerator {

    public List<EvaluationInsight> generateInsights(
            List<PredictionEvaluation> evaluations,
            NumericalEvaluationMetrics numMetrics,
            ClassificationEvaluationMetrics classMetrics,
            List<EvaluationConfidenceTier> confidenceTiers
    ) {
        List<PredictionEvaluation> evals = evaluations != null ? evaluations : Collections.emptyList();
        if (evals.isEmpty() && numMetrics == null && classMetrics == null && (confidenceTiers == null || confidenceTiers.isEmpty())) {
            return Collections.emptyList();
        }

        List<EvaluationInsight> insights = new ArrayList<>();

        // 1. Systematic Bias Insight
        if (numMetrics != null && numMetrics.isSufficientSample() && numMetrics.meanBiasError() != null) {
            double bias = numMetrics.meanBiasError().doubleValue();
            if (bias > 8.0) {
                insights.add(new EvaluationInsight(
                        UUID.randomUUID().toString(),
                        "BIAS",
                        "WARNING",
                        "Persistent Over-Prediction Bias",
                        String.format("Forecasts overestimated actual values by an average of +%s units in the evaluated period.", numMetrics.meanBiasError()),
                        "Mean Bias Error (MBE): +" + numMetrics.meanBiasError() + ", evaluated sample n = " + numMetrics.sampleSize(),
                        "Review crop coefficient (Kc) and soil moisture depletion rate parameters.",
                        "predictions",
                        "settings"
                ));
            } else if (bias < -8.0) {
                insights.add(new EvaluationInsight(
                        UUID.randomUUID().toString(),
                        "BIAS",
                        "WARNING",
                        "Persistent Under-Prediction Bias",
                        String.format("Forecasts systematically underestimated actual outcomes by an average of %s units.", numMetrics.meanBiasError()),
                        "Mean Bias Error (MBE): " + numMetrics.meanBiasError() + ", evaluated sample n = " + numMetrics.sampleSize(),
                        "Inspect reference evapotranspiration (ET0) assumptions and infiltration rate.",
                        "predictions",
                        "settings"
                ));
            }
        }

        // 2. Safety Interlock Impact Insight (Operational Context)
        long interruptedCount = evals.stream()
                .filter(e -> e.isSafetyInterlockTriggered() || e.getFailureReason() == EvaluationFailureReason.SAFETY_EVENT)
                .count();

        if (interruptedCount > 0) {
            insights.add(new EvaluationInsight(
                    UUID.randomUUID().toString(),
                    "SAFETY",
                    "INFO",
                    "Operational Safety Blocks Recognized",
                    String.format("%d predicted irrigation needs were halted by hydraulic safety interlocks rather than model failure.", interruptedCount),
                    interruptedCount + " operations classified as OUTCOME_INTERRUPTED instead of false positives.",
                    "Review hydraulic pressure anomalies and distribution canal schedule.",
                    "operations",
                    "safety"
            ));
        }

        // 3. Telemetry Bottleneck Insight
        long missingTelemetryCount = evals.stream()
                .filter(e -> e.getDataQuality() == EvaluationDataQuality.DATA_INSUFFICIENT
                        || e.getFailureReason() == EvaluationFailureReason.MISSING_SENSOR
                        || e.getFailureReason() == EvaluationFailureReason.STALE_TELEMETRY)
                .count();

        if (missingTelemetryCount >= 3) {
            insights.add(new EvaluationInsight(
                    UUID.randomUUID().toString(),
                    "TELEMETRY",
                    "WARNING",
                    "Sensor Connectivity Limiting Evaluation",
                    String.format("%d predictions could not be fully verified due to missing or stale sensor telemetry.", missingTelemetryCount),
                    missingTelemetryCount + " evaluations resulted in INSUFFICIENT_DATA due to missing sensor observations.",
                    "Inspect soil moisture probe battery levels and telemetry gateway transmission.",
                    "sensors",
                    "diagnostics"
            ));
        }

        // 4. Confidence Calibration Inversion
        if (confidenceTiers != null && confidenceTiers.size() >= 2) {
            EvaluationConfidenceTier highTier = confidenceTiers.stream().filter(t -> "HIGH".equals(t.tierName())).findFirst().orElse(null);
            EvaluationConfidenceTier lowOrMedTier = confidenceTiers.stream().filter(t -> "MEDIUM".equals(t.tierName()) || "LOW".equals(t.tierName())).findFirst().orElse(null);

            if (highTier != null && highTier.isSufficientSample() && lowOrMedTier != null && lowOrMedTier.isSufficientSample()) {
                if (highTier.meanAbsoluteError() != null && lowOrMedTier.meanAbsoluteError() != null
                        && highTier.meanAbsoluteError().compareTo(lowOrMedTier.meanAbsoluteError()) > 0) {
                    insights.add(new EvaluationInsight(
                            UUID.randomUUID().toString(),
                            "CALIBRATION",
                            "WARNING",
                            "Confidence Calibration Inversion",
                            "High-confidence forecasts exhibited higher average error than medium/low-confidence forecasts.",
                            String.format("High-tier MAE (%s) was worse than lower-tier MAE (%s).", highTier.meanAbsoluteError(), lowOrMedTier.meanAbsoluteError()),
                            "Audit data sufficiency weights in the confidence scoring engine.",
                            "predictions",
                            "calibration"
                    ));
                }
            }
        }

        return insights;
    }
}
