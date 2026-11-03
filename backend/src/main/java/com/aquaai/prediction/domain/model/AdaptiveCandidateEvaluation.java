package com.aquaai.prediction.domain.model;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Diagnostic evaluation of a specific candidate prediction method version considered
 * during the adaptive selection algorithm.
 */
@Builder
public record AdaptiveCandidateEvaluation(
        String versionTag,
        MethodVersionStatus status,
        boolean isEligible,
        String ineligibilityReason,
        ContextMatchLevel contextMatchLevel,
        int sampleSize,
        boolean hasSufficientSample,
        BigDecimal meanAbsoluteError,
        BigDecimal meanBiasError,
        ModelHealthStatus contextualHealth,
        BigDecimal selectionScore,
        String rejectionReason
) {
    public static AdaptiveCandidateEvaluation ineligible(
            String versionTag,
            MethodVersionStatus status,
            String reason
    ) {
        return AdaptiveCandidateEvaluation.builder()
                .versionTag(versionTag)
                .status(status)
                .isEligible(false)
                .ineligibilityReason(reason)
                .contextMatchLevel(ContextMatchLevel.INSUFFICIENT_DATA)
                .sampleSize(0)
                .hasSufficientSample(false)
                .rejectionReason(reason)
                .build();
    }
}
