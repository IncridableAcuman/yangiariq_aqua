package com.aquaai.intelligence.application.dto;

import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceRecommendation;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IntelligenceRecommendationResponse(
        UUID id,
        UUID fieldId,
        String recommendationType,
        IntelligenceSeverity priority,
        String reason,
        BigDecimal recommendedVolumeM3,
        Integer recommendedDurationMinutes,
        Instant recommendedWindowStart,
        Instant recommendedWindowEnd,
        IntelligenceConfidence confidence,
        List<String> constraints,
        List<AgronomicSignalType> supportingSignals,
        String status,
        Instant generatedAt
) {
    public static IntelligenceRecommendationResponse fromDomain(IntelligenceRecommendation domain) {
        if (domain == null) return null;
        return new IntelligenceRecommendationResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getRecommendationType(),
                domain.getPriority(),
                domain.getReason(),
                domain.getRecommendedVolumeM3(),
                domain.getRecommendedDurationMinutes(),
                domain.getRecommendedWindowStart(),
                domain.getRecommendedWindowEnd(),
                domain.getConfidence(),
                domain.getConstraints(),
                domain.getSupportingSignals(),
                domain.getStatus(),
                domain.getGeneratedAt()
        );
    }
}
