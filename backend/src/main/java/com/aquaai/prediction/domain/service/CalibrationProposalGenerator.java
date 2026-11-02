package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.CalibrationProposal;
import com.aquaai.prediction.domain.model.EvaluationInsight;
import com.aquaai.prediction.domain.model.GovernanceRiskLevel;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Service generating evidence-backed calibration proposals when persistent degradation
 * or systematic bias is detected.
 * <p>
 * STRICT SAFETY PRINCIPLE:
 * Proposes calibrations only for soft agronomic/forecasting parameters (Kc, smoothing, trend windows).
 * Hard hydraulic safety interlocks and emergency cutoff rules can NEVER be proposed or modified.
 */
public class CalibrationProposalGenerator {

    public Optional<CalibrationProposal> generateProposalIfDegraded(
            PredictionType predictionType,
            PredictionMethodVersion currentVersion,
            NumericalEvaluationMetrics metrics,
            boolean performanceDriftDetected,
            String driftSummary
    ) {
        if (currentVersion == null || metrics == null || !metrics.isSufficientSample()) {
            return Optional.empty();
        }

        BigDecimal mbe = metrics.meanBiasError();
        boolean hasHighPositiveBias = mbe != null && mbe.compareTo(BigDecimal.valueOf(6.0)) > 0;
        boolean hasHighNegativeBias = mbe != null && mbe.compareTo(BigDecimal.valueOf(-6.0)) < 0;

        if (!hasHighPositiveBias && !hasHighNegativeBias && !performanceDriftDetected) {
            return Optional.empty(); // Model is healthy, no calibration needed
        }

        String nextVersionTag = deriveNextVersionTag(currentVersion.getVersionTag());
        String proposalCode = "PROP-" + System.currentTimeMillis() % 100000;

        if (hasHighPositiveBias) {
            return Optional.of(CalibrationProposal.create(
                    proposalCode,
                    predictionType,
                    currentVersion.getVersionTag(),
                    nextVersionTag,
                    String.format("Persistent over-prediction bias (MBE: +%s) detected in %s.", mbe, predictionType),
                    String.format("Sample size n = %d, Baseline MAE = %s, Mean Bias Error = +%s.", metrics.sampleSize(), metrics.meanAbsoluteError(), mbe),
                    "Calibrate soft crop coefficient (Kc) reference multiplier from 1.00 down to 0.92 to eliminate over-estimation.",
                    "{\"kcMultiplier\":0.92,\"smoothingAlpha\":0.35,\"trendWindowHours\":24}",
                    "Expected reduction in water allocation over-estimation by 15-20% without increasing deficit risk.",
                    GovernanceRiskLevel.LOW,
                    "AUTOMATED_DRIFT_DETECTOR"
            ));
        }

        if (hasHighNegativeBias) {
            return Optional.of(CalibrationProposal.create(
                    proposalCode,
                    predictionType,
                    currentVersion.getVersionTag(),
                    nextVersionTag,
                    String.format("Persistent under-prediction bias (MBE: %s) detected in %s.", mbe, predictionType),
                    String.format("Sample size n = %d, Baseline MAE = %s, Mean Bias Error = %s.", metrics.sampleSize(), metrics.meanAbsoluteError(), mbe),
                    "Calibrate soft crop coefficient (Kc) reference multiplier from 1.00 up to 1.08 to prevent soil moisture stress.",
                    "{\"kcMultiplier\":1.08,\"smoothingAlpha\":0.35,\"trendWindowHours\":24}",
                    "Expected correction of soil moisture deficit trajectory under high summer evaporative demand.",
                    GovernanceRiskLevel.MEDIUM,
                    "AUTOMATED_DRIFT_DETECTOR"
            ));
        }

        if (performanceDriftDetected) {
            return Optional.of(CalibrationProposal.create(
                    proposalCode,
                    predictionType,
                    currentVersion.getVersionTag(),
                    nextVersionTag,
                    "Automated performance drift detected: error variance exceeded 30% baseline threshold.",
                    driftSummary != null ? driftSummary : "Recent error rate is >30% higher than 30-day historical baseline.",
                    "Recalibrate depletion velocity smoothing parameters and reduce rolling window sensitivity.",
                    "{\"tuningFactor\":0.94,\"smoothingAlpha\":0.40,\"trendWindowHours\":18}",
                    "Stabilizes forecast variance and dampens transient sensor noise.",
                    GovernanceRiskLevel.MEDIUM,
                    "AUTOMATED_DRIFT_DETECTOR"
            ));
        }

        return Optional.empty();
    }

    private String deriveNextVersionTag(String currentTag) {
        if (currentTag == null) return "V2";
        if (currentTag.endsWith("_V1")) return currentTag.replace("_V1", "_V2");
        if (currentTag.endsWith("_V2")) return currentTag.replace("_V2", "_V3");
        if (currentTag.endsWith("_V3")) return currentTag.replace("_V3", "_V4");
        return currentTag + "_CALIBRATED";
    }
}
