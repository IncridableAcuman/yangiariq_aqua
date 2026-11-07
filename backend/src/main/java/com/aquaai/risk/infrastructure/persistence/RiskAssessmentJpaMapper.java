package com.aquaai.risk.infrastructure.persistence;

import com.aquaai.risk.domain.model.FarmRiskAssessment;
import com.aquaai.risk.domain.model.RiskAssessmentLifecycleStatus;
import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskFactor;
import com.aquaai.risk.domain.model.RiskRecommendation;
import com.aquaai.risk.domain.model.RiskSeverity;
import com.aquaai.risk.domain.model.RiskTrend;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiskAssessmentJpaMapper {

    private final ObjectMapper objectMapper;

    public RiskAssessmentJpaEntity toJpaEntity(FarmRiskAssessment domain) {
        if (domain == null) return null;

        String factorsJson = "[]";
        String recsJson = "[]";
        String warningsJson = "[]";

        try {
            factorsJson = objectMapper.writeValueAsString(domain.getRiskFactors());
            recsJson = objectMapper.writeValueAsString(domain.getRecommendations());
            warningsJson = objectMapper.writeValueAsString(domain.getMissingDataWarnings());
        } catch (Exception e) {
            log.error("Failed to serialize risk assessment details to JSON: id={}", domain.getId(), e);
        }

        return RiskAssessmentJpaEntity.builder()
                .id(domain.getId())
                .fieldId(domain.getFieldId())
                .fieldName(domain.getFieldName())
                .overallSeverity(domain.getOverallSeverity().name())
                .overallConfidence(domain.getOverallConfidence().name())
                .riskTrend(domain.getRiskTrend().name())
                .riskScore(domain.getRiskScore())
                .isHydraulicBlocked(domain.isHydraulicBlocked())
                .hydraulicBlockReason(domain.getHydraulicBlockReason())
                .primaryRiskCategory(domain.getPrimaryRiskCategory().name())
                .lifecycleStatus(domain.getLifecycleStatus().name())
                .priorityRank(domain.getPriorityRank())
                .priorityReason(domain.getPriorityReason())
                .riskFactorsJson(factorsJson)
                .recommendationsJson(recsJson)
                .missingDataWarningsJson(warningsJson)
                .evaluatedAt(domain.getEvaluatedAt())
                .acknowledgedAt(domain.getAcknowledgedAt())
                .acknowledgedBy(domain.getAcknowledgedBy())
                .resolvedAt(domain.getResolvedAt())
                .resolvedBy(domain.getResolvedBy())
                .resolutionNotes(domain.getResolutionNotes())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public FarmRiskAssessment toDomain(RiskAssessmentJpaEntity entity) {
        if (entity == null) return null;

        List<RiskFactor> factors = Collections.emptyList();
        List<RiskRecommendation> recommendations = Collections.emptyList();
        List<String> warnings = Collections.emptyList();

        try {
            if (entity.getRiskFactorsJson() != null && !entity.getRiskFactorsJson().isBlank()) {
                factors = objectMapper.readValue(entity.getRiskFactorsJson(), new TypeReference<List<RiskFactor>>() {});
            }
            if (entity.getRecommendationsJson() != null && !entity.getRecommendationsJson().isBlank()) {
                recommendations = objectMapper.readValue(entity.getRecommendationsJson(), new TypeReference<List<RiskRecommendation>>() {});
            }
            if (entity.getMissingDataWarningsJson() != null && !entity.getMissingDataWarningsJson().isBlank()) {
                warnings = objectMapper.readValue(entity.getMissingDataWarningsJson(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to deserialize risk assessment JSON: id={}", entity.getId(), e);
        }

        RiskSeverity severity;
        try {
            severity = RiskSeverity.valueOf(entity.getOverallSeverity());
        } catch (Exception e) {
            severity = RiskSeverity.UNKNOWN;
        }

        RiskConfidence confidence;
        try {
            confidence = RiskConfidence.valueOf(entity.getOverallConfidence());
        } catch (Exception e) {
            confidence = RiskConfidence.INSUFFICIENT_DATA;
        }

        RiskTrend trend;
        try {
            trend = RiskTrend.valueOf(entity.getRiskTrend());
        } catch (Exception e) {
            trend = RiskTrend.UNKNOWN;
        }

        RiskCategory primaryCategory;
        try {
            primaryCategory = RiskCategory.valueOf(entity.getPrimaryRiskCategory());
        } catch (Exception e) {
            primaryCategory = RiskCategory.WATER_AVAILABILITY;
        }

        RiskAssessmentLifecycleStatus status;
        try {
            status = RiskAssessmentLifecycleStatus.valueOf(entity.getLifecycleStatus());
        } catch (Exception e) {
            status = RiskAssessmentLifecycleStatus.OPEN;
        }

        return new FarmRiskAssessment(
                entity.getId(),
                entity.getFieldId(),
                entity.getFieldName(),
                severity,
                confidence,
                trend,
                entity.getRiskScore(),
                entity.isHydraulicBlocked(),
                entity.getHydraulicBlockReason(),
                primaryCategory,
                status,
                entity.getPriorityRank(),
                entity.getPriorityReason(),
                factors,
                recommendations,
                warnings,
                entity.getEvaluatedAt(),
                entity.getAcknowledgedAt(),
                entity.getAcknowledgedBy(),
                entity.getResolvedAt(),
                entity.getResolvedBy(),
                entity.getResolutionNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
