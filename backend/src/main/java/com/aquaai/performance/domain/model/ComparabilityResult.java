package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Result of rigorous comparability evaluation between two operational periods or datasets.
 * If periods are not comparable, comparisonValid is false with explicit reasons.
 */
@Getter
@Builder
public class ComparabilityResult {
    private final boolean comparisonValid;
    private final String reason;
    private final List<String> compatibilityChecks;

    public static ComparabilityResult valid(List<String> passedChecks) {
        return ComparabilityResult.builder()
                .comparisonValid(true)
                .reason("Periods and scopes are fully comparable across duration, season, unit, and methodology.")
                .compatibilityChecks(passedChecks != null ? passedChecks : Collections.emptyList())
                .build();
    }

    public static ComparabilityResult invalid(String failureReason, List<String> checks) {
        return ComparabilityResult.builder()
                .comparisonValid(false)
                .reason("COMPARISON_NOT_VALID: " + failureReason)
                .compatibilityChecks(checks != null ? checks : Collections.emptyList())
                .build();
    }
}
