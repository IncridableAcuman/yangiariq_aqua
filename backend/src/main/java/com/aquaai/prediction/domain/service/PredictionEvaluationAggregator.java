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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain Service responsible for aggregate metric calculations across evaluated predictions.
 * <p>
 * Strictly implements metric governance and the minimum sample size policy:
 * - No fake or fabricated percentages when sample size is below minimum threshold.
 * - Safe zero-denominator behavior for MAPE, Precision, Recall, and F1.
 * - Clear distinction between numerical error and categorical confusion matrix.
 */
public class PredictionEvaluationAggregator {

    public static final int DEFAULT_MINIMUM_SAMPLE_SIZE = 10;
    private static final MathContext MC = new MathContext(6, RoundingMode.HALF_UP);

    public record ModelHealthResult(
            ModelHealthStatus status,
            List<String> reasons
    ) {}

    public NumericalEvaluationMetrics calculateNumericalMetrics(List<PredictionEvaluation> evaluations) {
        return calculateNumericalMetrics(evaluations, DEFAULT_MINIMUM_SAMPLE_SIZE);
    }

    public NumericalEvaluationMetrics calculateNumericalMetrics(List<PredictionEvaluation> evaluations, int minimumSampleSize) {
        if (evaluations == null || evaluations.isEmpty()) {
            return NumericalEvaluationMetrics.empty(minimumSampleSize);
        }

        List<PredictionEvaluation> valid = evaluations.stream()
                .filter(e -> e.getEvaluationStatus() == EvaluationStatus.EVALUATED)
                .filter(e -> e.getAbsoluteError() != null && e.getPredictedValue() != null && e.getActualValue() != null)
                .toList();

        int n = valid.size();
        if (n == 0) {
            return NumericalEvaluationMetrics.empty(minimumSampleSize);
        }

        int withinBoundsCount = (int) valid.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsWithinBounds()))
                .count();

        BigDecimal withinBoundsPct = BigDecimal.valueOf(withinBoundsCount)
                .divide(BigDecimal.valueOf(n), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        if (n < minimumSampleSize) {
            return new NumericalEvaluationMetrics(
                    n,
                    minimumSampleSize,
                    false,
                    null,
                    null,
                    null,
                    false,
                    null,
                    withinBoundsCount,
                    withinBoundsPct
            );
        }

        // 1. MAE = sum(|predicted - actual|) / n
        BigDecimal sumAbsError = valid.stream()
                .map(PredictionEvaluation::getAbsoluteError)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mae = sumAbsError.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        // 2. RMSE = sqrt(sum((predicted - actual)^2) / n)
        BigDecimal sumSquaredError = BigDecimal.ZERO;
        for (PredictionEvaluation e : valid) {
            BigDecimal diff = e.getPredictedValue().subtract(e.getActualValue());
            sumSquaredError = sumSquaredError.add(diff.multiply(diff));
        }
        BigDecimal meanSquaredError = sumSquaredError.divide(BigDecimal.valueOf(n), MC);
        BigDecimal rmse = BigDecimal.valueOf(Math.sqrt(meanSquaredError.doubleValue()))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. MBE (Mean Bias Error) = sum(predicted - actual) / n
        BigDecimal sumDirError = valid.stream()
                .map(PredictionEvaluation::getDirectionalError)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mbe = sumDirError.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);

        // 4. Safe MAPE: undefined/not applicable if any actualValue == 0
        boolean hasZeroActual = valid.stream().anyMatch(e -> e.getActualValue().compareTo(BigDecimal.ZERO) == 0);
        BigDecimal mape = null;
        boolean isMapeApplicable = !hasZeroActual;

        if (isMapeApplicable) {
            BigDecimal sumPercentageError = BigDecimal.ZERO;
            for (PredictionEvaluation e : valid) {
                BigDecimal pe = e.getAbsoluteError().divide(e.getActualValue().abs(), MC);
                sumPercentageError = sumPercentageError.add(pe);
            }
            mape = sumPercentageError.divide(BigDecimal.valueOf(n), MC)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new NumericalEvaluationMetrics(
                n,
                minimumSampleSize,
                true,
                mae,
                rmse,
                mape,
                isMapeApplicable,
                mbe,
                withinBoundsCount,
                withinBoundsPct
        );
    }

    public ClassificationEvaluationMetrics calculateClassificationMetrics(List<PredictionEvaluation> evaluations) {
        return calculateClassificationMetrics(evaluations, DEFAULT_MINIMUM_SAMPLE_SIZE);
    }

    public ClassificationEvaluationMetrics calculateClassificationMetrics(List<PredictionEvaluation> evaluations, int minimumSampleSize) {
        if (evaluations == null || evaluations.isEmpty()) {
            return ClassificationEvaluationMetrics.empty(minimumSampleSize);
        }

        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;
        int interrupted = 0;

        for (PredictionEvaluation e : evaluations) {
            if (e.getClassificationResult() == PredictionClassificationResult.TRUE_POSITIVE) {
                tp++;
            } else if (e.getClassificationResult() == PredictionClassificationResult.TRUE_NEGATIVE) {
                tn++;
            } else if (e.getClassificationResult() == PredictionClassificationResult.FALSE_POSITIVE) {
                fp++;
            } else if (e.getClassificationResult() == PredictionClassificationResult.FALSE_NEGATIVE) {
                fn++;
            } else if (e.getClassificationResult() == PredictionClassificationResult.INTERRUPTED) {
                interrupted++;
            }
        }

        int totalEvaluated = tp + tn + fp + fn;
        if (totalEvaluated < minimumSampleSize) {
            return new ClassificationEvaluationMetrics(
                    totalEvaluated,
                    minimumSampleSize,
                    false,
                    tp,
                    tn,
                    fp,
                    fn,
                    interrupted,
                    null,
                    null,
                    null,
                    null
            );
        }

        // Accuracy = (TP + TN) / (TP + TN + FP + FN)
        BigDecimal accuracy = BigDecimal.valueOf(tp + tn)
                .divide(BigDecimal.valueOf(totalEvaluated), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // Precision = TP / (TP + FP)
        BigDecimal precision = null;
        if (tp + fp > 0) {
            precision = BigDecimal.valueOf(tp)
                    .divide(BigDecimal.valueOf(tp + fp), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Recall = TP / (TP + FN)
        BigDecimal recall = null;
        if (tp + fn > 0) {
            recall = BigDecimal.valueOf(tp)
                    .divide(BigDecimal.valueOf(tp + fn), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // F1 = 2 * (Precision * Recall) / (Precision + Recall)
        BigDecimal f1 = null;
        if (precision != null && recall != null && precision.add(recall).compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal num = BigDecimal.valueOf(2).multiply(precision).multiply(recall);
            BigDecimal den = precision.add(recall);
            f1 = num.divide(den, 2, RoundingMode.HALF_UP);
        }

        return new ClassificationEvaluationMetrics(
                totalEvaluated,
                minimumSampleSize,
                true,
                tp,
                tn,
                fp,
                fn,
                interrupted,
                accuracy,
                precision,
                recall,
                f1
        );
    }

    public List<EvaluationConfidenceTier> calculateConfidenceTiers(List<PredictionEvaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return Collections.emptyList();
        }

        List<PredictionEvaluation> highTier = evaluations.stream().filter(e -> e.getConfidencePercent() >= 75).toList();
        List<PredictionEvaluation> medTier = evaluations.stream().filter(e -> e.getConfidencePercent() >= 50 && e.getConfidencePercent() < 75).toList();
        List<PredictionEvaluation> lowTier = evaluations.stream().filter(e -> e.getConfidencePercent() < 50).toList();

        List<EvaluationConfidenceTier> result = new ArrayList<>();
        result.add(buildTier("HIGH", 75, 100, highTier));
        result.add(buildTier("MEDIUM", 50, 74, medTier));
        result.add(buildTier("LOW", 0, 49, lowTier));

        return result;
    }

    private EvaluationConfidenceTier buildTier(String name, int min, int max, List<PredictionEvaluation> list) {
        int n = list.size();
        boolean sufficient = n >= 5; // tier min sample

        BigDecimal mae = null;
        BigDecimal accOrF1 = null;

        if (sufficient) {
            NumericalEvaluationMetrics num = calculateNumericalMetrics(list, 5);
            if (num.isSufficientSample()) {
                mae = num.meanAbsoluteError();
            }

            ClassificationEvaluationMetrics cl = calculateClassificationMetrics(list, 5);
            if (cl.isSufficientSample()) {
                accOrF1 = cl.f1ScorePercent() != null ? cl.f1ScorePercent() : cl.accuracyPercent();
            }
        }

        String status = "UNVERIFIED";
        if (sufficient) {
            status = "CALIBRATED";
        }

        return new EvaluationConfidenceTier(name, min, max, n, sufficient, mae, accOrF1, status);
    }

    public List<EvaluationDataQualityImpact> calculateDataQualityImpact(List<PredictionEvaluation> evaluations) {
        if (evaluations == null || evaluations.isEmpty()) {
            return Collections.emptyList();
        }

        List<PredictionEvaluation> complete = evaluations.stream().filter(e -> e.getDataQuality() == EvaluationDataQuality.DATA_COMPLETE).toList();
        List<PredictionEvaluation> partial = evaluations.stream().filter(e -> e.getDataQuality() == EvaluationDataQuality.DATA_PARTIAL).toList();
        List<PredictionEvaluation> insufficient = evaluations.stream().filter(e -> e.getDataQuality() == EvaluationDataQuality.DATA_INSUFFICIENT).toList();

        List<EvaluationDataQualityImpact> list = new ArrayList<>();
        list.add(buildQualityImpact(EvaluationDataQuality.DATA_COMPLETE, complete, "Complete telemetry: optimal sensor reporting within defined tolerance."));
        list.add(buildQualityImpact(EvaluationDataQuality.DATA_PARTIAL, partial, "Partial telemetry: reading delay or suspect readings observed."));
        list.add(buildQualityImpact(EvaluationDataQuality.DATA_INSUFFICIENT, insufficient, "Insufficient telemetry: observation window missing or expired."));

        return list;
    }

    private EvaluationDataQualityImpact buildQualityImpact(EvaluationDataQuality q, List<PredictionEvaluation> items, String defaultDesc) {
        int n = items.size();
        boolean sufficient = n >= 5;
        BigDecimal mae = null;
        BigDecimal acc = null;

        if (sufficient) {
            NumericalEvaluationMetrics num = calculateNumericalMetrics(items, 5);
            if (num.isSufficientSample()) {
                mae = num.meanAbsoluteError();
            }
            ClassificationEvaluationMetrics cl = calculateClassificationMetrics(items, 5);
            if (cl.isSufficientSample()) {
                acc = cl.accuracyPercent();
            }
        }

        return new EvaluationDataQualityImpact(q, n, sufficient, mae, acc, defaultDesc);
    }

    public ModelHealthResult determineModelHealth(
            NumericalEvaluationMetrics numMetrics,
            ClassificationEvaluationMetrics classMetrics,
            int totalEvaluated,
            int minimumSample
    ) {
        List<String> reasons = new ArrayList<>();

        if (totalEvaluated < minimumSample) {
            reasons.add(String.format("Insufficient evaluation sample size (n = %d, minimum required = %d).", totalEvaluated, minimumSample));
            return new ModelHealthResult(ModelHealthStatus.INSUFFICIENT_DATA, reasons);
        }

        boolean hasDegradation = false;
        boolean hasWarning = false;

        // Numerical checks
        if (numMetrics != null && numMetrics.isSufficientSample()) {
            if (numMetrics.meanBiasError() != null) {
                double bias = Math.abs(numMetrics.meanBiasError().doubleValue());
                if (bias > 15.0) {
                    reasons.add(String.format("Significant directional forecast bias detected (MBE: %s).", numMetrics.meanBiasError()));
                    hasDegradation = true;
                } else if (bias > 8.0) {
                    reasons.add(String.format("Moderate forecast bias observed (MBE: %s).", numMetrics.meanBiasError()));
                    hasWarning = true;
                }
            }

            if (numMetrics.withinBoundsPercentage() != null && numMetrics.withinBoundsPercentage().doubleValue() < 60.0) {
                reasons.add(String.format("Forecast bounds capture rate is low (%s%% within bounds).", numMetrics.withinBoundsPercentage()));
                hasWarning = true;
            }
        }

        // Classification checks
        if (classMetrics != null && classMetrics.isSufficientSample()) {
            if (classMetrics.f1ScorePercent() != null && classMetrics.f1ScorePercent().doubleValue() < 60.0) {
                reasons.add(String.format("Classification F1 score is degraded (%s%%).", classMetrics.f1ScorePercent()));
                hasDegradation = true;
            } else if (classMetrics.accuracyPercent() != null && classMetrics.accuracyPercent().doubleValue() < 70.0) {
                reasons.add(String.format("Classification accuracy below nominal threshold (%s%%).", classMetrics.accuracyPercent()));
                hasWarning = true;
            }
        }

        if (hasDegradation) {
            return new ModelHealthResult(ModelHealthStatus.DEGRADED, reasons);
        }
        if (hasWarning) {
            return new ModelHealthResult(ModelHealthStatus.WATCH, reasons);
        }

        reasons.add("Predictive models operating within nominal agronomic error and accuracy boundaries.");
        return new ModelHealthResult(ModelHealthStatus.HEALTHY, reasons);
    }

    public EvaluationDriftSummary calculateDrift(
            List<PredictionEvaluation> currentPeriod,
            List<PredictionEvaluation> previousPeriod,
            int minimumSample
    ) {
        NumericalEvaluationMetrics currentNum = calculateNumericalMetrics(currentPeriod, minimumSample);
        NumericalEvaluationMetrics previousNum = calculateNumericalMetrics(previousPeriod, minimumSample);

        if (!currentNum.isSufficientSample() || !previousNum.isSufficientSample()) {
            return new EvaluationDriftSummary(
                    "MAE",
                    currentNum.meanAbsoluteError(),
                    previousNum.meanAbsoluteError(),
                    null,
                    false,
                    ModelHealthStatus.INSUFFICIENT_DATA,
                    "Insufficient sample size across comparable periods to evaluate drift."
            );
        }

        BigDecimal currMae = currentNum.meanAbsoluteError();
        BigDecimal prevMae = previousNum.meanAbsoluteError();

        if (prevMae.compareTo(BigDecimal.ZERO) == 0) {
            return new EvaluationDriftSummary("MAE", currMae, prevMae, BigDecimal.ZERO, false, ModelHealthStatus.HEALTHY, "Stable performance.");
        }

        BigDecimal diff = currMae.subtract(prevMae);
        BigDecimal pctChange = diff.divide(prevMae, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        boolean drift = pctChange.doubleValue() > 25.0; // MAE increased by > 25%
        ModelHealthStatus status = drift ? ModelHealthStatus.DEGRADED : (pctChange.doubleValue() > 10.0 ? ModelHealthStatus.WATCH : ModelHealthStatus.HEALTHY);

        String explanation = drift
                ? String.format("Performance drift detected: average error increased by %s%% compared to preceding period.", pctChange)
                : String.format("Model error change (%s%%) remains within acceptable variance.", pctChange);

        return new EvaluationDriftSummary("MAE", currMae, prevMae, pctChange, drift, status, explanation);
    }
}
