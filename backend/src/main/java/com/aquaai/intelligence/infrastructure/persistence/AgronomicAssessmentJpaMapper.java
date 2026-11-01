package com.aquaai.intelligence.infrastructure.persistence;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.AgronomicSignal;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;
import com.aquaai.intelligence.domain.model.TelemetryFreshness;
import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.model.TrendDirection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgronomicAssessmentJpaMapper {

    public AgronomicAssessmentJpaEntity toEntity(AgronomicAssessment domain) {
        if (domain == null) return null;

        AgronomicAssessmentJpaEntity entity = AgronomicAssessmentJpaEntity.builder()
                .id(domain.getId())
                .fieldId(domain.getFieldId())
                .primarySignal(domain.getPrimarySignal().name())
                .overallSeverity(domain.getOverallSeverity().name())
                .soilMoistureTrendDirection(domain.getSoilMoistureTrend().getDirection().name())
                .soilMoistureRatePerHour(domain.getSoilMoistureTrend().getRateOfChangePerHour())
                .telemetryFreshness(domain.getTelemetryFreshness().name())
                .confidence(domain.getConfidence().name())
                .confidenceScore(domain.getConfidenceScore())
                .agronomicSummary(domain.getAgronomicSummary())
                .recommendationsOverview(domain.getRecommendationsOverview())
                .hydraulicSafetyBlocked(domain.isHydraulicSafetyBlocked())
                .hydraulicSafetyReason(domain.getHydraulicSafetyReason())
                .waterBudgetConstrained(domain.isWaterBudgetConstrained())
                .waterBudgetReason(domain.getWaterBudgetReason())
                .assessedAt(domain.getAssessedAt())
                .createdAt(domain.getCreatedAt())
                .build();

        if (domain.getSignals() != null) {
            for (AgronomicSignal signal : domain.getSignals()) {
                AgronomicAssessmentSignalJpaEntity signalEntity = AgronomicAssessmentSignalJpaEntity.builder()
                        .id(signal.getId())
                        .assessment(entity)
                        .signalType(signal.getSignalType().name())
                        .severity(signal.getSeverity().name())
                        .metric(signal.getMetric())
                        .observedValue(signal.getObservedValue())
                        .thresholdValue(signal.getThresholdValue())
                        .message(signal.getMessage())
                        .createdAt(domain.getCreatedAt())
                        .build();
                entity.addSignal(signalEntity);
            }
        }

        return entity;
    }

    public AgronomicAssessment toDomain(AgronomicAssessmentJpaEntity entity) {
        if (entity == null) return null;

        List<AgronomicSignal> signals = new ArrayList<>();
        if (entity.getSignals() != null) {
            for (AgronomicAssessmentSignalJpaEntity s : entity.getSignals()) {
                signals.add(new AgronomicSignal(
                        s.getId(),
                        AgronomicSignalType.valueOf(s.getSignalType()),
                        IntelligenceSeverity.valueOf(s.getSeverity()),
                        s.getMetric(),
                        s.getObservedValue(),
                        s.getThresholdValue(),
                        s.getMessage()
                ));
            }
        }

        TrendDirection trendDirection = TrendDirection.valueOf(entity.getSoilMoistureTrendDirection());
        TelemetryTrend trend = new TelemetryTrend(
                trendDirection,
                entity.getSoilMoistureRatePerHour(),
                signals.size(),
                null,
                entity.getAssessedAt()
        );

        return new AgronomicAssessment(
                entity.getId(),
                entity.getFieldId(),
                AgronomicSignalType.valueOf(entity.getPrimarySignal()),
                IntelligenceSeverity.valueOf(entity.getOverallSeverity()),
                trend,
                TelemetryFreshness.valueOf(entity.getTelemetryFreshness()),
                IntelligenceConfidence.valueOf(entity.getConfidence()),
                entity.getConfidenceScore(),
                entity.getAgronomicSummary(),
                entity.getRecommendationsOverview(),
                entity.isHydraulicSafetyBlocked(),
                entity.getHydraulicSafetyReason(),
                entity.isWaterBudgetConstrained(),
                entity.getWaterBudgetReason(),
                entity.getAssessedAt(),
                entity.getCreatedAt(),
                signals
        );
    }
}
