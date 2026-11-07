package com.aquaai.risk.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Concise read-model representation of a field parcel's active risk status.
 * Optimized for tables, overview cards, and sorting.
 */
@Builder
public record FieldRiskProfile(
        UUID assessmentId,
        UUID fieldId,
        String fieldName,
        BigDecimal areaHectares,
        String cropType,
        RiskSeverity overallSeverity,
        RiskConfidence overallConfidence,
        RiskTrend riskTrend,
        BigDecimal riskScore,
        boolean isHydraulicBlocked,
        String hydraulicBlockReason,
        RiskCategory primaryRiskCategory,
        RiskAssessmentLifecycleStatus lifecycleStatus,
        int priorityRank,
        String priorityReason,
        int activeFactorCount,
        int recommendationCount,
        List<String> missingDataWarnings,
        Instant evaluatedAt
) {
}
