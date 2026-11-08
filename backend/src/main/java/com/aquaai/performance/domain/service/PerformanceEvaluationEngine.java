package com.aquaai.performance.domain.service;

import com.aquaai.performance.domain.model.PerformanceBaseline;
import com.aquaai.performance.domain.model.PerformanceDataQuality;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import com.aquaai.performance.domain.model.PerformanceTarget;
import com.aquaai.performance.domain.model.PerformanceTrend;
import com.aquaai.performance.domain.model.TargetComplianceStatus;
import com.aquaai.performance.domain.model.TargetType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Pure domain engine generating reproducible, immutable performance evaluation snapshots.
 */
public class PerformanceEvaluationEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private final IndicatorCalculationEngine calculationEngine;
    private final BaselineComparabilityEngine comparabilityEngine;

    public PerformanceEvaluationEngine(
            IndicatorCalculationEngine calculationEngine,
            BaselineComparabilityEngine comparabilityEngine
    ) {
        this.calculationEngine = calculationEngine;
        this.comparabilityEngine = comparabilityEngine;
    }

    public PerformanceEvaluation evaluate(
            PerformanceIndicator indicator,
            PerformanceBaseline baseline,
            PerformanceTarget target,
            BigDecimal calculatedValue,
            Instant periodStart,
            Instant periodEnd,
            PerformanceDataQuality dataQuality,
            PerformanceTrend detectedTrend,
            String calculationVersion
    ) {
        // Evaluate target compliance
        TargetComplianceStatus compliance = evaluateTargetCompliance(target, calculatedValue);

        // Evaluate variance from baseline
        BigDecimal varianceFromBaseline = null;
        boolean comparisonValid = true;
        String validityNotes = null;

        if (baseline != null) {
            var compResult = comparabilityEngine.evaluateComparability(
                    baseline,
                    indicator.getSupportedGeographicScope(),
                    null,
                    periodStart,
                    periodEnd,
                    indicator.getMeasurementUnit(),
                    baseline.getSeasonContext()
            );
            comparisonValid = compResult.isComparisonValid();
            validityNotes = compResult.getReason();

            if (comparisonValid) {
                varianceFromBaseline = calculationEngine.calculatePercentageChange(calculatedValue, baseline.getBaselineValue());
            }
        }

        // Evaluate variance from target
        BigDecimal varianceFromTarget = null;
        if (target != null && target.getTargetValue() != null) {
            varianceFromTarget = calculationEngine.calculatePercentageChange(calculatedValue, target.getTargetValue());
        }

        String summary = generateSummary(indicator, calculatedValue, compliance, varianceFromBaseline, varianceFromTarget, dataQuality);

        return PerformanceEvaluation.builder()
                .id(UUID.randomUUID())
                .evaluationNumber("EVAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .indicatorCode(indicator.getCode())
                .scopeType(indicator.getSupportedGeographicScope())
                .scopeId(baseline != null ? baseline.getScopeId() : null)
                .scopeName(baseline != null ? baseline.getScopeName() : "District Wide")
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .calculatedValue(calculatedValue)
                .measurementUnit(indicator.getMeasurementUnit())
                .baselineId(baseline != null ? baseline.getId() : null)
                .baselineValue(baseline != null ? baseline.getBaselineValue() : null)
                .targetId(target != null ? target.getId() : null)
                .targetValue(target != null ? target.getTargetValue() : null)
                .targetCompliance(compliance)
                .varianceFromBaselinePct(varianceFromBaseline)
                .varianceFromTargetPct(varianceFromTarget)
                .trend(detectedTrend != null ? detectedTrend : PerformanceTrend.STABLE)
                .evidenceClassification(dataQuality != null ? dataQuality.getOverallClassification() : indicator.getEvidenceRequirement().contains("OBSERVED") ? com.aquaai.performance.domain.model.EvidenceClassification.OBSERVED : com.aquaai.performance.domain.model.EvidenceClassification.CALCULATED)
                .observedSampleRatio(dataQuality != null ? dataQuality.getObservedCoveragePercent() : new BigDecimal("100.00"))
                .missingDataIntervals(dataQuality != null ? (int) dataQuality.getMissingIntervalCount() : 0)
                .comparisonValid(comparisonValid)
                .validityNotes(validityNotes)
                .explanatorySummary(summary)
                .calculationVersion(calculationVersion != null ? calculationVersion : indicator.getCalculationVersion())
                .evaluatedAt(Instant.now())
                .build();
    }

    public TargetComplianceStatus evaluateTargetCompliance(PerformanceTarget target, BigDecimal actualValue) {
        if (target == null || actualValue == null) {
            return TargetComplianceStatus.INSUFFICIENT_DATA;
        }

        TargetType type = target.getTargetType();
        BigDecimal targetVal = target.getTargetValue();

        switch (type) {
            case MINIMUM:
                if (actualValue.compareTo(targetVal) >= 0) {
                    return TargetComplianceStatus.MET;
                } else if (actualValue.compareTo(targetVal.multiply(new BigDecimal("0.95"))) >= 0) {
                    return TargetComplianceStatus.WITHIN_TOLERANCE;
                } else {
                    return TargetComplianceStatus.UNMET;
                }
            case MAXIMUM:
                if (actualValue.compareTo(targetVal) <= 0) {
                    return TargetComplianceStatus.MET;
                } else if (actualValue.compareTo(targetVal.multiply(new BigDecimal("1.05"))) <= 0) {
                    return TargetComplianceStatus.WITHIN_TOLERANCE;
                } else {
                    return TargetComplianceStatus.EXCEEDED;
                }
            case RANGE:
                BigDecimal min = target.getRangeMin() != null ? target.getRangeMin() : targetVal;
                BigDecimal max = target.getRangeMax() != null ? target.getRangeMax() : targetVal;
                if (actualValue.compareTo(min) >= 0 && actualValue.compareTo(max) <= 0) {
                    return TargetComplianceStatus.MET;
                } else if (actualValue.compareTo(min.multiply(new BigDecimal("0.95"))) >= 0 &&
                           actualValue.compareTo(max.multiply(new BigDecimal("1.05"))) <= 0) {
                    return TargetComplianceStatus.WITHIN_TOLERANCE;
                } else if (actualValue.compareTo(min) < 0) {
                    return TargetComplianceStatus.UNMET;
                } else {
                    return TargetComplianceStatus.EXCEEDED;
                }
            case DIRECTIONAL_IMPROVEMENT:
            default:
                return TargetComplianceStatus.MET;
        }
    }

    private String generateSummary(
            PerformanceIndicator indicator,
            BigDecimal value,
            TargetComplianceStatus compliance,
            BigDecimal varianceBaseline,
            BigDecimal varianceTarget,
            PerformanceDataQuality dataQuality
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(indicator.getName())
                .append(": evaluated at ")
                .append(value)
                .append(" ")
                .append(indicator.getMeasurementUnit().getSymbol())
                .append(". ");

        if (compliance != TargetComplianceStatus.INSUFFICIENT_DATA) {
            sb.append("Target status: ").append(compliance).append(". ");
        }
        if (varianceBaseline != null) {
            sb.append("Variance from baseline: ")
                    .append(varianceBaseline.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "")
                    .append(varianceBaseline)
                    .append("%. ");
        }
        if (dataQuality != null) {
            sb.append("Data completeness: ")
                    .append(dataQuality.getObservedCoveragePercent())
                    .append("% observed (")
                    .append(dataQuality.getOverallClassification())
                    .append(").");
        }
        return sb.toString();
    }
}
