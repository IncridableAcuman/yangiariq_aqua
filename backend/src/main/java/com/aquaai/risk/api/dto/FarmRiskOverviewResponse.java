package com.aquaai.risk.api.dto;

import com.aquaai.risk.domain.model.RiskCategory;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record FarmRiskOverviewResponse(
        int totalAssessedFields,
        int criticalRiskCount,
        int highRiskCount,
        int mediumRiskCount,
        int lowRiskCount,
        int insufficientDataCount,
        int hydraulicEmergencyCount,
        int waterShortageCount,
        int severeMoistureRiskCount,
        int telemetryIssuesCount,
        int modelReliabilityWarningsCount,
        int activeRecommendationsCount,
        Map<RiskCategory, Integer> categoryDistribution,
        List<FieldRiskProfileResponse> criticalEmergencyFields,
        List<RiskRecommendationResponse> topPrioritizedRecommendations,
        Instant evaluatedAt
) {
}
