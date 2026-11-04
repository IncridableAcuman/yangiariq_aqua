package com.aquaai.prediction.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service executing the hybrid residual prediction strategy.
 * <p>
 * Ensures the critical safety principle:
 * ML predicts -> Agronomic & physical rules constrain -> Safety dominates.
 * <p>
 * If ML residual exceeds bounded limits or produces unphysical results,
 * the correction is safely rejected and the platform reverts to the deterministic baseline.
 */
public class HybridPredictionEngine {

    public static final double DEFAULT_MAX_RESIDUAL_CORRECTION = 5.0; // max +/- 5.0% soil moisture correction
    public static final double MIN_SOIL_MOISTURE_BOUND = 5.0;         // Permanent wilting threshold
    public static final double MAX_SOIL_MOISTURE_BOUND = 55.0;        // Field saturation threshold

    public record HybridResult(
            double finalPrediction,
            double baselinePrediction,
            double mlPrediction,
            double appliedCorrection,
            boolean fallbackUsed,
            String rationale
    ) {
        public BigDecimal toBigDecimalFinal() {
            return BigDecimal.valueOf(finalPrediction).setScale(1, RoundingMode.HALF_UP);
        }
    }

    public HybridResult computeHybridPrediction(
            double baselinePrediction,
            Double mlPrediction,
            Double maxCorrectionLimit,
            boolean criticalSafetyActive
    ) {
        // 1. Critical safety interlock dominance
        if (criticalSafetyActive) {
            return new HybridResult(
                    baselinePrediction,
                    baselinePrediction,
                    mlPrediction != null ? mlPrediction : baselinePrediction,
                    0.0,
                    true,
                    "Safety interlock active on parcel; deterministic baseline authoritative."
            );
        }

        // 2. ML Failure or null check -> fallback
        if (mlPrediction == null || Double.isNaN(mlPrediction) || Double.isInfinite(mlPrediction)) {
            return new HybridResult(
                    baselinePrediction,
                    baselinePrediction,
                    baselinePrediction,
                    0.0,
                    true,
                    "ML candidate output unavailable; safe deterministic fallback applied."
            );
        }

        double maxCorr = maxCorrectionLimit != null ? Math.abs(maxCorrectionLimit) : DEFAULT_MAX_RESIDUAL_CORRECTION;

        // 3. Raw residual
        double rawResidual = Math.round((mlPrediction - baselinePrediction) * 100.0) / 100.0;

        // 4. Residual bound check
        if (Math.abs(rawResidual) > maxCorr) {
            // Reject anomalous correction that exceeds agronomic tolerance
            return new HybridResult(
                    baselinePrediction,
                    baselinePrediction,
                    mlPrediction,
                    0.0,
                    true,
                    String.format("ML residual (%.1f%%) exceeded safe agronomic limit (±%.1f%%); rejected, fell back to baseline.",
                            rawResidual, maxCorr)
            );
        }

        // 5. Compute proposed hybrid prediction
        double proposed = baselinePrediction + rawResidual;

        // 6. Absolute physical bounds clamp [5.0%, 55.0%]
        if (proposed < MIN_SOIL_MOISTURE_BOUND || proposed > MAX_SOIL_MOISTURE_BOUND) {
            return new HybridResult(
                    baselinePrediction,
                    baselinePrediction,
                    mlPrediction,
                    0.0,
                    true,
                    String.format("Proposed hybrid prediction (%.1f%%) breached physical moisture bounds; fallback to baseline.", proposed)
            );
        }

        return new HybridResult(
                proposed,
                baselinePrediction,
                mlPrediction,
                rawResidual,
                false,
                String.format("Bounded ML residual correction of %s%.1f%% applied to baseline (%.1f%% -> %.1f%%).",
                        rawResidual >= 0 ? "+" : "", rawResidual, baselinePrediction, proposed)
        );
    }
}
