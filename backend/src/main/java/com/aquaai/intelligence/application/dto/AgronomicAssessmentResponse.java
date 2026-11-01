package com.aquaai.intelligence.application.dto;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.AgronomicSignalType;
import com.aquaai.intelligence.domain.model.IntelligenceConfidence;
import com.aquaai.intelligence.domain.model.IntelligenceSeverity;
import com.aquaai.intelligence.domain.model.TelemetryFreshness;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record AgronomicAssessmentResponse(
        UUID id,
        UUID fieldId,
        AgronomicSignalType primarySignal,
        IntelligenceSeverity overallSeverity,
        TelemetryTrendResponse soilMoistureTrend,
        TelemetryFreshness telemetryFreshness,
        IntelligenceConfidence confidence,
        BigDecimal confidenceScore,
        String agronomicSummary,
        String recommendationsOverview,
        boolean hydraulicSafetyBlocked,
        String hydraulicSafetyReason,
        boolean waterBudgetConstrained,
        String waterBudgetReason,
        Instant assessedAt,
        List<AgronomicSignalResponse> signals
) {
    public static AgronomicAssessmentResponse fromDomain(AgronomicAssessment domain) {
        if (domain == null) return null;

        List<AgronomicSignalResponse> signalResponses = domain.getSignals() != null
                ? domain.getSignals().stream().map(AgronomicSignalResponse::fromDomain).toList()
                : Collections.emptyList();

        return new AgronomicAssessmentResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getPrimarySignal(),
                domain.getOverallSeverity(),
                TelemetryTrendResponse.fromDomain(domain.getSoilMoistureTrend()),
                domain.getTelemetryFreshness(),
                domain.getConfidence(),
                domain.getConfidenceScore(),
                domain.getAgronomicSummary(),
                domain.getRecommendationsOverview(),
                domain.isHydraulicSafetyBlocked(),
                domain.getHydraulicSafetyReason(),
                domain.isWaterBudgetConstrained(),
                domain.getWaterBudgetReason(),
                domain.getAssessedAt(),
                signalResponses
        );
    }
}
