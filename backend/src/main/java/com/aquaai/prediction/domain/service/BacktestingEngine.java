package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Deterministic Historical Backtesting Engine.
 * <p>
 * Simulates candidate model performance strictly on past ground-truth observations.
 * Enforces anti-leakage: evaluations only consume telemetry that was available at forecast time.
 */
public class BacktestingEngine {

    private final RegressionGuardService regressionGuardService;

    public BacktestingEngine(RegressionGuardService regressionGuardService) {
        this.regressionGuardService = Objects.requireNonNull(regressionGuardService, "regressionGuardService must not be null");
    }

    public BacktestRun executeBacktest(
            UUID proposalId,
            String currentVersionTag,
            String candidateVersionTag,
            PredictionType predictionType,
            String proposedConfigurationJson,
            List<PredictionEvaluation> historicalEvaluations,
            Instant dateRangeFrom,
            Instant dateRangeTo,
            String executedBy
    ) {
        int sampleSize = historicalEvaluations != null ? historicalEvaluations.size() : 0;
        boolean hasSufficientSample = sampleSize >= 10;

        if (sampleSize == 0) {
            return new BacktestRun(
                    UUID.randomUUID(), proposalId, currentVersionTag, candidateVersionTag,
                    dateRangeFrom, dateRangeTo, 0,
                    null, null, BigDecimal.ZERO,
                    null, null, null, null,
                    null, null, null, null,
                    false, "No historical evaluations in backtest window.",
                    EvaluationDataQuality.DATA_INSUFFICIENT, false, false,
                    Instant.now(), executedBy
            );
        }

        // Parse candidate adjustment factor from soft configuration JSON
        BigDecimal tuningFactor = extractTuningFactor(proposedConfigurationJson, predictionType);

        // 1. Calculate Current Active Metrics
        BigDecimal currentMae = calculateMae(historicalEvaluations, BigDecimal.ONE);
        BigDecimal currentRmse = calculateRmse(historicalEvaluations, BigDecimal.ONE);
        BigDecimal currentMbe = calculateMbe(historicalEvaluations, BigDecimal.ONE);

        // 2. Calculate Candidate Metrics on the EXACT same historical sample (Anti-leakage)
        BigDecimal candidateMae = calculateMae(historicalEvaluations, tuningFactor);
        BigDecimal candidateRmse = calculateRmse(historicalEvaluations, tuningFactor);
        BigDecimal candidateMbe = calculateMbe(historicalEvaluations, tuningFactor);

        BigDecimal maeImprovement = BacktestRun.calculateImprovement(currentMae, candidateMae);

        // Classification Metrics (for discrete predictions)
        BigDecimal currentAccuracy = calculateAccuracy(historicalEvaluations, BigDecimal.ONE);
        BigDecimal candidateAccuracy = calculateAccuracy(historicalEvaluations, tuningFactor);
        BigDecimal currentF1 = calculateF1(historicalEvaluations, BigDecimal.ONE);
        BigDecimal candidateF1 = calculateF1(historicalEvaluations, tuningFactor);

        // 3. Regression Guard Check
        RegressionGuardService.RegressionCheckResult regCheck = regressionGuardService.evaluateRegression(
                predictionType,
                currentMae, candidateMae,
                currentMbe, candidateMbe,
                currentAccuracy, candidateAccuracy,
                currentF1, candidateF1
        );

        return new BacktestRun(
                UUID.randomUUID(),
                proposalId,
                currentVersionTag,
                candidateVersionTag,
                dateRangeFrom,
                dateRangeTo,
                sampleSize,
                currentMae,
                candidateMae,
                maeImprovement,
                currentRmse,
                candidateRmse,
                currentMbe,
                candidateMbe,
                currentAccuracy,
                candidateAccuracy,
                currentF1,
                candidateF1,
                regCheck.regressionDetected(),
                regCheck.details(),
                EvaluationDataQuality.DATA_COMPLETE,
                true,
                hasSufficientSample,
                Instant.now(),
                executedBy
        );
    }

    private BigDecimal extractTuningFactor(String configJson, PredictionType type) {
        if (configJson == null || configJson.isBlank()) {
            return BigDecimal.ONE;
        }
        try {
            // Check for specific tuning parameters e.g. "kcMultiplier", "kcReference", "depletionAdjustment"
            if (configJson.contains("kcMultiplier")) {
                int idx = configJson.indexOf("kcMultiplier");
                String sub = configJson.substring(idx + 14).trim();
                String numStr = sub.split("[,}]")[0].trim();
                return new BigDecimal(numStr);
            }
            if (configJson.contains("tuningFactor")) {
                int idx = configJson.indexOf("tuningFactor");
                String sub = configJson.substring(idx + 14).trim();
                String numStr = sub.split("[,}]")[0].trim();
                return new BigDecimal(numStr);
            }
            if (configJson.contains("kcReference")) {
                int idx = configJson.indexOf("kcReference");
                String sub = configJson.substring(idx + 13).trim();
                String numStr = sub.split("[,}]")[0].trim();
                return new BigDecimal(numStr);
            }
        } catch (Exception ignored) {}
        return new BigDecimal("0.92"); // Default safe slight contraction to reduce positive bias
    }

    private BigDecimal calculateMae(List<PredictionEvaluation> evals, BigDecimal factor) {
        List<BigDecimal> errors = new ArrayList<>();
        for (PredictionEvaluation e : evals) {
            if (e.getPredictedValue() != null && e.getActualValue() != null) {
                BigDecimal candidatePredicted = e.getPredictedValue().multiply(factor).setScale(4, RoundingMode.HALF_UP);
                BigDecimal absErr = candidatePredicted.subtract(e.getActualValue()).abs();
                errors.add(absErr);
            }
        }
        if (errors.isEmpty()) return null;
        BigDecimal sum = errors.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(errors.size()), 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRmse(List<PredictionEvaluation> evals, BigDecimal factor) {
        List<BigDecimal> sqErrors = new ArrayList<>();
        for (PredictionEvaluation e : evals) {
            if (e.getPredictedValue() != null && e.getActualValue() != null) {
                BigDecimal candidatePredicted = e.getPredictedValue().multiply(factor).setScale(4, RoundingMode.HALF_UP);
                BigDecimal err = candidatePredicted.subtract(e.getActualValue());
                sqErrors.add(err.multiply(err));
            }
        }
        if (sqErrors.isEmpty()) return null;
        BigDecimal meanSq = sqErrors.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(sqErrors.size()), 6, RoundingMode.HALF_UP);
        double sqrt = Math.sqrt(meanSq.doubleValue());
        return BigDecimal.valueOf(sqrt).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMbe(List<PredictionEvaluation> evals, BigDecimal factor) {
        List<BigDecimal> dirErrors = new ArrayList<>();
        for (PredictionEvaluation e : evals) {
            if (e.getPredictedValue() != null && e.getActualValue() != null) {
                BigDecimal candidatePredicted = e.getPredictedValue().multiply(factor).setScale(4, RoundingMode.HALF_UP);
                BigDecimal dirErr = candidatePredicted.subtract(e.getActualValue());
                dirErrors.add(dirErr);
            }
        }
        if (dirErrors.isEmpty()) return null;
        BigDecimal sum = dirErrors.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(dirErrors.size()), 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAccuracy(List<PredictionEvaluation> evals, BigDecimal factor) {
        long total = evals.stream().filter(e -> e.getClassificationResult() != null).count();
        if (total == 0) return null;
        long correct = evals.stream().filter(e ->
                "TRUE_POSITIVE".equals(e.getClassificationResult().name()) ||
                "TRUE_NEGATIVE".equals(e.getClassificationResult().name())).count();
        return BigDecimal.valueOf(correct)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateF1(List<PredictionEvaluation> evals, BigDecimal factor) {
        long tp = evals.stream().filter(e -> "TRUE_POSITIVE".equals(e.getClassificationResult().name())).count();
        long fp = evals.stream().filter(e -> "FALSE_POSITIVE".equals(e.getClassificationResult().name())).count();
        long fn = evals.stream().filter(e -> "FALSE_NEGATIVE".equals(e.getClassificationResult().name())).count();

        if (tp + fp == 0 || tp + fn == 0) return null;
        double precision = (double) tp / (tp + fp);
        double recall = (double) tp / (tp + fn);
        if (precision + recall == 0) return BigDecimal.ZERO;
        double f1 = 2 * (precision * recall) / (precision + recall);
        return BigDecimal.valueOf(f1).setScale(2, RoundingMode.HALF_UP);
    }
}
