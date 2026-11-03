package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.ContextPerformanceProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Detects contextual performance drift where a predictive method version may be healthy globally,
 * but exhibits localized degradation under specific crop, soil, irrigation, or telemetry contexts.
 */
public class ContextualDriftDetector {

    private static final BigDecimal DRIFT_THRESHOLD_RATIO = BigDecimal.valueOf(1.30); // 30% error increase
    private static final BigDecimal DRIFT_BIAS_THRESHOLD = BigDecimal.valueOf(8.0);   // +/-8% bias swing

    public record DriftEvaluation(
            boolean isDriftDetected,
            BigDecimal errorDegradationPercent,
            String diagnosis
    ) {}

    public DriftEvaluation evaluate(
            ContextPerformanceProfile contextualProfile,
            BigDecimal globalMae,
            BigDecimal globalMbe
    ) {
        if (contextualProfile == null || !contextualProfile.hasSufficientSample()) {
            return new DriftEvaluation(false, BigDecimal.ZERO, "Insufficient contextual sample size (n < 10) for drift evaluation.");
        }

        BigDecimal ctxMae = contextualProfile.meanAbsoluteError();
        BigDecimal ctxMbe = contextualProfile.meanBiasError();

        // 1. Error magnitude comparison
        if (ctxMae != null && globalMae != null && globalMae.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = ctxMae.divide(globalMae, 4, RoundingMode.HALF_UP);
            if (ratio.compareTo(DRIFT_THRESHOLD_RATIO) >= 0) {
                BigDecimal increasePct = ratio.subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100.0))
                        .setScale(1, RoundingMode.HALF_UP);

                String diag = String.format("Contextual MAE is %s%% higher than global baseline for %s under %s irrigation.",
                        increasePct, contextualProfile.cropType(), contextualProfile.irrigationMethod());
                return new DriftEvaluation(true, increasePct, diag);
            }
        }

        // 2. Bias shift check
        if (ctxMbe != null) {
            BigDecimal biasDiff = globalMbe != null ? ctxMbe.subtract(globalMbe).abs() : ctxMbe.abs();
            if (biasDiff.compareTo(DRIFT_BIAS_THRESHOLD) >= 0) {
                String diag = String.format("Contextual Mean Bias Error (%s%.1f%%) deviates significantly from baseline for %s.",
                        ctxMbe.compareTo(BigDecimal.ZERO) > 0 ? "+" : "", ctxMbe, contextualProfile.cropType());
                return new DriftEvaluation(true, BigDecimal.ZERO, diag);
            }
        }

        return new DriftEvaluation(false, BigDecimal.ZERO, "Contextual performance is consistent with global baseline.");
    }

    public ContextPerformanceProfile detectDrift(ContextPerformanceProfile profile, BigDecimal globalMae, BigDecimal globalMbe) {
        if (profile == null) return null;
        DriftEvaluation eval = evaluate(profile, globalMae, globalMbe);
        if (eval.isDriftDetected()) {
            return ContextPerformanceProfile.builder()
                    .predictionType(profile.predictionType())
                    .versionTag(profile.versionTag())
                    .cropType(profile.cropType())
                    .soilType(profile.soilType())
                    .irrigationMethod(profile.irrigationMethod())
                    .telemetryQuality(profile.telemetryQuality())
                    .season(profile.season())
                    .sampleSize(profile.sampleSize())
                    .hasSufficientSample(profile.hasSufficientSample())
                    .meanAbsoluteError(profile.meanAbsoluteError())
                    .rootMeanSquaredError(profile.rootMeanSquaredError())
                    .meanBiasError(profile.meanBiasError())
                    .accuracyPercent(profile.accuracyPercent())
                    .globalModelHealth(profile.globalModelHealth())
                    .contextualModelHealth(com.aquaai.prediction.domain.model.ModelHealthStatus.DEGRADED)
                    .contextDriftDetected(true)
                    .driftDiagnosis(eval.diagnosis())
                    .build();
        }
        return profile;
    }
}
