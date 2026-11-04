package com.aquaai.prediction.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Side-by-side comparison between the authoritative deterministic baseline
 * and the candidate ML model on the identical unseen test split.
 */
public record BaselineComparisonMetrics(
        double baselineMae,
        double mlMae,
        double maeImprovementPercent,
        double baselineRmse,
        double mlRmse,
        double rmseImprovementPercent,
        double baselineMbe,
        double mlMbe,
        int sampleSize,
        boolean regressionDetected,
        String regressionReason,
        String recommendationStatus
) {
    public static BaselineComparisonMetrics evaluate(
            ModelMetrics baselineMetrics,
            ModelMetrics mlMetrics,
            int minRequiredTestSamples
    ) {
        if (baselineMetrics == null || mlMetrics == null || mlMetrics.sampleSize() < minRequiredTestSamples) {
            return new BaselineComparisonMetrics(
                    baselineMetrics != null ? baselineMetrics.mae() : 0.0,
                    mlMetrics != null ? mlMetrics.mae() : 0.0,
                    0.0,
                    baselineMetrics != null ? baselineMetrics.rmse() : 0.0,
                    mlMetrics != null ? mlMetrics.rmse() : 0.0,
                    0.0,
                    baselineMetrics != null ? baselineMetrics.mbe() : 0.0,
                    mlMetrics != null ? mlMetrics.mbe() : 0.0,
                    mlMetrics != null ? mlMetrics.sampleSize() : 0,
                    true,
                    "Insufficient test sample size (n < " + minRequiredTestSamples + ")",
                    "INSUFFICIENT_EVIDENCE"
            );
        }

        double baseMae = baselineMetrics.mae();
        double mlMae = mlMetrics.mae();
        double maeImprovement = 0.0;
        if (baseMae > 1e-6) {
            maeImprovement = ((baseMae - mlMae) / baseMae) * 100.0;
        }

        double baseRmse = baselineMetrics.rmse();
        double mlRmse = mlMetrics.rmse();
        double rmseImprovement = 0.0;
        if (baseRmse > 1e-6) {
            rmseImprovement = ((baseRmse - mlRmse) / baseRmse) * 100.0;
        }

        boolean regression = false;
        StringBuilder reason = new StringBuilder();

        // Regression guard: if ML is worse than baseline by > 3% MAE or RMSE
        if (mlMae > baseMae * 1.03) {
            regression = true;
            reason.append(String.format("ML MAE (%.2f) degraded by >3%% relative to baseline (%.2f). ", mlMae, baseMae));
        }

        if (mlRmse > baseRmse * 1.03) {
            regression = true;
            reason.append(String.format("ML RMSE (%.2f) degraded by >3%% relative to baseline (%.2f). ", mlRmse, baseRmse));
        }

        // Check systematic bias inflation
        if (Math.abs(mlMetrics.mbe()) > Math.abs(baselineMetrics.mbe()) + 4.0) {
            regression = true;
            reason.append(String.format("ML Mean Bias Error (%.2f) shows unacceptable bias drift compared to baseline (%.2f). ",
                    mlMetrics.mbe(), baselineMetrics.mbe()));
        }

        String recStatus;
        if (regression) {
            recStatus = "NOT_RECOMMENDED";
        } else if (maeImprovement >= 5.0) {
            recStatus = "RECOMMENDED";
            reason.append(String.format("Demonstrates statistically significant accuracy gain (%.1f%% MAE reduction).", maeImprovement));
        } else {
            recStatus = "MARGINAL_IMPROVEMENT";
            reason.append(String.format("Comparable performance to deterministic baseline (%.1f%% difference).", maeImprovement));
        }

        return new BaselineComparisonMetrics(
                baseMae,
                mlMae,
                BigDecimal.valueOf(maeImprovement).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                baseRmse,
                mlRmse,
                BigDecimal.valueOf(rmseImprovement).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                baselineMetrics.mbe(),
                mlMetrics.mbe(),
                mlMetrics.sampleSize(),
                regression,
                reason.toString().trim(),
                recStatus
        );
    }
}
