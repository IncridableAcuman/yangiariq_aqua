package com.aquaai.prediction.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic outcome of the context-aware adaptive method selection algorithm.
 * Contains the selected version, configuration snapshot, matching confidence, and an explainable rationale.
 */
@Builder
public record AdaptiveSelectionResult(
        PredictionType predictionType,
        UUID fieldId,
        PredictionContext context,
        String selectedMethod,
        String selectedVersionTag,
        String configurationSnapshot,
        ContextMatchLevel contextMatchLevel,
        int sampleSize,
        String selectionReason,
        BigDecimal selectionScore,
        int confidencePercent,
        String evidenceSummary,
        List<AdaptiveCandidateEvaluation> alternativesConsidered,
        String limitations,
        Instant selectedAt
) {
    public static AdaptiveSelectionResult fallback(
            PredictionType predictionType,
            UUID fieldId,
            PredictionContext context,
            String fallbackVersionTag,
            String configSnapshot,
            String reason,
            int confidencePercent
    ) {
        return AdaptiveSelectionResult.builder()
                .predictionType(predictionType)
                .fieldId(fieldId)
                .context(context)
                .selectedMethod(predictionType.name() + "_DEFAULT")
                .selectedVersionTag(fallbackVersionTag)
                .configurationSnapshot(configSnapshot)
                .contextMatchLevel(ContextMatchLevel.BASELINE_FALLBACK)
                .sampleSize(0)
                .selectionReason(reason)
                .selectionScore(BigDecimal.valueOf(50.0))
                .confidencePercent(confidencePercent)
                .evidenceSummary("Fallback to active baseline due to insufficient context evidence.")
                .alternativesConsidered(List.of())
                .limitations("Context-specific evidence insufficient for tailored model selection.")
                .selectedAt(Instant.now())
                .build();
    }
}
