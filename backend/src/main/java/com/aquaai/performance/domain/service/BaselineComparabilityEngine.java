package com.aquaai.performance.domain.service;

import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.ComparabilityResult;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.PerformanceBaseline;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain service validating comparability between evaluation periods, baselines, and geographic scopes.
 * <p>
 * Non-negotiable standard:
 * If datasets or periods are not strictly comparable, it explicitly reports COMPARISON_NOT_VALID.
 */
public class BaselineComparabilityEngine {

    private static final BigDecimal MIN_COMPLETENESS_REQUIRED = new BigDecimal("70.00");
    private static final double MAX_DURATION_VARIANCE_RATIO = 0.30; // Max 30% difference in evaluation duration

    public ComparabilityResult evaluateComparability(
            PerformanceBaseline baseline,
            GeographicScopeType targetScopeType,
            UUID targetScopeId,
            Instant targetPeriodStart,
            Instant targetPeriodEnd,
            MeasurementUnit targetUnit,
            String targetSeasonContext
    ) {
        List<String> passedChecks = new ArrayList<>();

        if (baseline == null) {
            return ComparabilityResult.invalid("Missing baseline reference", passedChecks);
        }

        // 1. Quality Tier Check
        if (baseline.getBaselineQuality() == BaselineQuality.INSUFFICIENT_BASELINE) {
            return ComparabilityResult.invalid("Baseline quality is INSUFFICIENT_BASELINE with unverified or sparse data.", passedChecks);
        }
        passedChecks.add("Baseline quality: " + baseline.getBaselineQuality());

        // 2. Data Completeness Check
        if (baseline.getDataCompletenessPct() != null && baseline.getDataCompletenessPct().compareTo(MIN_COMPLETENESS_REQUIRED) < 0) {
            return ComparabilityResult.invalid("Baseline data completeness (" + baseline.getDataCompletenessPct() + "%) is below minimum requirement of " + MIN_COMPLETENESS_REQUIRED + "%.", passedChecks);
        }
        passedChecks.add("Data completeness threshold satisfied (" + baseline.getDataCompletenessPct() + "%)");

        // 3. Measurement Unit Compatibility
        if (targetUnit != null && baseline.getMeasurementUnit() != targetUnit) {
            return ComparabilityResult.invalid("Unit mismatch: baseline uses " + baseline.getMeasurementUnit() + " but target uses " + targetUnit + ".", passedChecks);
        }
        passedChecks.add("Measurement units compatible (" + baseline.getMeasurementUnit() + ")");

        // 4. Geographic Scope Compatibility
        if (targetScopeType != null && baseline.getScopeType() != targetScopeType) {
            if (baseline.getScopeType() != GeographicScopeType.DISTRICT && targetScopeType != GeographicScopeType.DISTRICT) {
                return ComparabilityResult.invalid("Incompatible geographic scope type: baseline scope is " + baseline.getScopeType() + " while evaluation is " + targetScopeType + ".", passedChecks);
            }
        }
        if (targetScopeId != null && baseline.getScopeId() != null && !Objects.equals(baseline.getScopeId(), targetScopeId)) {
            return ComparabilityResult.invalid("Geographic entity mismatch: baseline is for ID " + baseline.getScopeId() + " (" + baseline.getScopeName() + ") while target is for " + targetScopeId + ".", passedChecks);
        }
        passedChecks.add("Geographic scope matched (" + baseline.getScopeName() + ")");

        // 5. Duration Equivalence
        if (targetPeriodStart != null && targetPeriodEnd != null) {
            long baselineDurationSeconds = Math.abs(Duration.between(baseline.getPeriodStart(), baseline.getPeriodEnd()).getSeconds());
            long targetDurationSeconds = Math.abs(Duration.between(targetPeriodStart, targetPeriodEnd).getSeconds());

            if (baselineDurationSeconds <= 0 || targetDurationSeconds <= 0) {
                return ComparabilityResult.invalid("Zero or negative period duration encountered.", passedChecks);
            }

            double durationRatio = Math.abs((double) baselineDurationSeconds - (double) targetDurationSeconds) / (double) Math.max(baselineDurationSeconds, targetDurationSeconds);
            if (durationRatio > MAX_DURATION_VARIANCE_RATIO) {
                return ComparabilityResult.invalid("Duration mismatch: baseline duration (" + (baselineDurationSeconds / 86400) + " days) differs from target period (" + (targetDurationSeconds / 86400) + " days) by more than 30%.", passedChecks);
            }
            passedChecks.add("Duration equivalence confirmed (within " + (int)(MAX_DURATION_VARIANCE_RATIO * 100) + "% tolerance)");
        }

        // 6. Seasonal Context Compatibility
        if (targetSeasonContext != null && baseline.getSeasonContext() != null) {
            if (!baseline.getSeasonContext().equalsIgnoreCase(targetSeasonContext)) {
                return ComparabilityResult.invalid("Seasonal context mismatch: cannot compare '" + baseline.getSeasonContext() + "' with '" + targetSeasonContext + "'.", passedChecks);
            }
            passedChecks.add("Seasonal context matched (" + baseline.getSeasonContext() + ")");
        }

        return ComparabilityResult.valid(passedChecks);
    }
}
