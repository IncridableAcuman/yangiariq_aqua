package com.aquaai.intelligence.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agronomic Assessment Aggregate Root representing the deterministically calculated
 * agricultural and hydraulic status of an agricultural parcel.
 */
@Getter
@Builder
public class AgronomicAssessment {

    private final UUID id;
    private final UUID fieldId;
    private final AgronomicSignalType primarySignal;
    private final IntelligenceSeverity overallSeverity;
    private final TelemetryTrend soilMoistureTrend;
    private final TelemetryFreshness telemetryFreshness;
    private final IntelligenceConfidence confidence;
    private final BigDecimal confidenceScore;
    private final String agronomicSummary;
    private final String recommendationsOverview;
    private final boolean hydraulicSafetyBlocked;
    private final String hydraulicSafetyReason;
    private final boolean waterBudgetConstrained;
    private final String waterBudgetReason;
    private final Instant assessedAt;
    private final Instant createdAt;
    @Builder.Default
    private final List<AgronomicSignal> signals = new ArrayList<>();

    public AgronomicAssessment(
            UUID id,
            UUID fieldId,
            AgronomicSignalType primarySignal,
            IntelligenceSeverity overallSeverity,
            TelemetryTrend soilMoistureTrend,
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
            Instant createdAt,
            List<AgronomicSignal> signals
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.primarySignal = Objects.requireNonNull(primarySignal, "primarySignal must not be null");
        this.overallSeverity = overallSeverity != null ? overallSeverity : IntelligenceSeverity.INFO;
        this.soilMoistureTrend = soilMoistureTrend != null
                ? soilMoistureTrend
                : TelemetryTrend.stable(0, null, null);
        this.telemetryFreshness = telemetryFreshness != null ? telemetryFreshness : TelemetryFreshness.FRESH;
        this.confidence = confidence != null ? confidence : IntelligenceConfidence.MEDIUM;
        this.confidenceScore = confidenceScore != null
                ? confidenceScore.setScale(4, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(0.75).setScale(4, RoundingMode.HALF_UP);
        this.agronomicSummary = Objects.requireNonNull(agronomicSummary, "agronomicSummary must not be null");
        this.recommendationsOverview = recommendationsOverview;
        this.hydraulicSafetyBlocked = hydraulicSafetyBlocked;
        this.hydraulicSafetyReason = hydraulicSafetyReason;
        this.waterBudgetConstrained = waterBudgetConstrained;
        this.waterBudgetReason = waterBudgetReason;
        this.assessedAt = assessedAt != null ? assessedAt : Instant.now();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.signals = signals != null ? new ArrayList<>(signals) : new ArrayList<>();
    }

    public List<AgronomicSignal> getSignals() {
        return Collections.unmodifiableList(signals);
    }
}
