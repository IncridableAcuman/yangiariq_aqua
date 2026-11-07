package com.aquaai.risk.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root representing the comprehensive, explainable risk assessment for a field parcel.
 * Fully captures multi-factor risk, supporting evidence, confidence, and human lifecycle state.
 */
@Getter
@Builder(toBuilder = true)
public class FarmRiskAssessment {

    private final UUID id;
    private final UUID fieldId;
    private final String fieldName;
    private RiskSeverity overallSeverity;
    private RiskConfidence overallConfidence;
    private RiskTrend riskTrend;
    private BigDecimal riskScore;
    private boolean isHydraulicBlocked;
    private String hydraulicBlockReason;
    private RiskCategory primaryRiskCategory;
    private RiskAssessmentLifecycleStatus lifecycleStatus;
    private int priorityRank;
    private String priorityReason;
    private final List<RiskFactor> riskFactors;
    private final List<RiskRecommendation> recommendations;
    private final List<String> missingDataWarnings;
    private final Instant evaluatedAt;
    private Instant acknowledgedAt;
    private UUID acknowledgedBy;
    private Instant resolvedAt;
    private UUID resolvedBy;
    private String resolutionNotes;
    private final Instant createdAt;
    private Instant updatedAt;

    public FarmRiskAssessment(
            UUID id,
            UUID fieldId,
            String fieldName,
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
            List<RiskFactor> riskFactors,
            List<RiskRecommendation> recommendations,
            List<String> missingDataWarnings,
            Instant evaluatedAt,
            Instant acknowledgedAt,
            UUID acknowledgedBy,
            Instant resolvedAt,
            UUID resolvedBy,
            String resolutionNotes,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.fieldName = fieldName != null ? fieldName : "Field";
        this.overallSeverity = overallSeverity != null ? overallSeverity : RiskSeverity.UNKNOWN;
        this.overallConfidence = overallConfidence != null ? overallConfidence : RiskConfidence.INSUFFICIENT_DATA;
        this.riskTrend = riskTrend != null ? riskTrend : RiskTrend.UNKNOWN;
        this.riskScore = riskScore != null ? riskScore : BigDecimal.ZERO;
        this.isHydraulicBlocked = isHydraulicBlocked;
        this.hydraulicBlockReason = hydraulicBlockReason;
        this.primaryRiskCategory = primaryRiskCategory != null ? primaryRiskCategory : RiskCategory.WATER_AVAILABILITY;
        this.lifecycleStatus = lifecycleStatus != null ? lifecycleStatus : RiskAssessmentLifecycleStatus.OPEN;
        this.priorityRank = priorityRank > 0 ? priorityRank : 9;
        this.priorityReason = priorityReason != null ? priorityReason : "Standard agricultural evaluation";
        this.riskFactors = riskFactors != null ? Collections.unmodifiableList(new ArrayList<>(riskFactors)) : Collections.emptyList();
        this.recommendations = recommendations != null ? Collections.unmodifiableList(new ArrayList<>(recommendations)) : Collections.emptyList();
        this.missingDataWarnings = missingDataWarnings != null ? Collections.unmodifiableList(new ArrayList<>(missingDataWarnings)) : Collections.emptyList();
        this.evaluatedAt = evaluatedAt != null ? evaluatedAt : Instant.now();
        this.acknowledgedAt = acknowledgedAt;
        this.acknowledgedBy = acknowledgedBy;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.resolutionNotes = resolutionNotes;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void acknowledge(UUID userId) {
        this.lifecycleStatus = RiskAssessmentLifecycleStatus.ACKNOWLEDGED;
        this.acknowledgedAt = Instant.now();
        this.acknowledgedBy = userId;
        this.updatedAt = Instant.now();
    }

    public void markActionRequired() {
        this.lifecycleStatus = RiskAssessmentLifecycleStatus.ACTION_REQUIRED;
        this.updatedAt = Instant.now();
    }

    public void markMitigated() {
        this.lifecycleStatus = RiskAssessmentLifecycleStatus.MITIGATED;
        this.updatedAt = Instant.now();
    }

    public void resolve(UUID userId, String notes) {
        this.lifecycleStatus = RiskAssessmentLifecycleStatus.RESOLVED;
        this.resolvedAt = Instant.now();
        this.resolvedBy = userId;
        this.resolutionNotes = notes;
        this.updatedAt = Instant.now();
    }

    public void expire() {
        this.lifecycleStatus = RiskAssessmentLifecycleStatus.EXPIRED;
        this.updatedAt = Instant.now();
    }
}
