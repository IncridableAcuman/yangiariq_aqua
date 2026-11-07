package com.aquaai.risk.api.dto;

import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.model.RiskTrend;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Builder
public record FieldRiskProfileResponse(
        UUID assessmentId,
        UUID fieldId,
        String fieldName,
        BigDecimal areaHectares,
        String cropType,
        String soilType,
        String irrigationMethod,
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
        List<RiskFactorResponse> riskFactors,
        List<RiskRecommendationResponse> recommendations,
        List<String> missingDataWarnings,
        Instant evaluatedAt,
        Instant acknowledgedAt,
        UUID acknowledgedBy,
        Instant resolvedAt,
        UUID resolvedBy,
        String resolutionNotes
) {
    public static FieldRiskProfileResponse fromDomain(
            FarmRiskAssessment a,
            BigDecimal areaHectares,
            String cropType,
            String soilType,
            String irrigationMethod
    ) {
        if (a == null) return null;

        List<RiskFactorResponse> factorDtos = a.getRiskFactors() != null
                ? a.getRiskFactors().stream().map(RiskFactorResponse::fromDomain).toList()
                : Collections.emptyList();

        List<RiskRecommendationResponse> recDtos = a.getRecommendations() != null
                ? a.getRecommendations().stream().map(RiskRecommendationResponse::fromDomain).toList()
                : Collections.emptyList();

        return FieldRiskProfileResponse.builder()
                .assessmentId(a.getId())
                .fieldId(a.getFieldId())
                .fieldName(a.getFieldName())
                .areaHectares(areaHectares)
                .cropType(cropType)
                .soilType(soilType)
                .irrigationMethod(irrigationMethod)
                .overallSeverity(a.getOverallSeverity())
                .overallConfidence(a.getOverallConfidence())
                .riskTrend(a.getRiskTrend())
                .riskScore(a.getRiskScore())
                .isHydraulicBlocked(a.isHydraulicBlocked())
                .hydraulicBlockReason(a.getHydraulicBlockReason())
                .primaryRiskCategory(a.getPrimaryRiskCategory())
                .lifecycleStatus(a.getLifecycleStatus())
                .priorityRank(a.getPriorityRank())
                .priorityReason(a.getPriorityReason())
                .activeFactorCount(factorDtos.size())
                .recommendationCount(recDtos.size())
                .riskFactors(factorDtos)
                .recommendations(recDtos)
                .missingDataWarnings(a.getMissingDataWarnings())
                .evaluatedAt(a.getEvaluatedAt())
                .acknowledgedAt(a.getAcknowledgedAt())
                .acknowledgedBy(a.getAcknowledgedBy())
                .resolvedAt(a.getResolvedAt())
                .resolvedBy(a.getResolvedBy())
                .resolutionNotes(a.getResolutionNotes())
                .build();
    }
}
