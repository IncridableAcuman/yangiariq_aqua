package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain model representing an empirical or approved operational baseline.
 */
@Getter
@Builder(toBuilder = true)
public class PerformanceBaseline {

    private final UUID id;
    private final String indicatorCode;
    private final String name;
    private final String description;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal baselineValue;
    private final MeasurementUnit measurementUnit;
    private final BigDecimal dataCompletenessPct;
    private final EvidenceClassification evidenceClassification;
    private final BaselineQuality baselineQuality;
    private final String calculationMethod;
    private final String seasonContext;
    private final String approvalStatus;
    private final UUID approvedBy;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PerformanceBaseline(
            UUID id,
            String indicatorCode,
            String name,
            String description,
            GeographicScopeType scopeType,
            UUID scopeId,
            String scopeName,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal baselineValue,
            MeasurementUnit measurementUnit,
            BigDecimal dataCompletenessPct,
            EvidenceClassification evidenceClassification,
            BaselineQuality baselineQuality,
            String calculationMethod,
            String seasonContext,
            String approvalStatus,
            UUID approvedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.indicatorCode = Objects.requireNonNull(indicatorCode, "indicatorCode must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.scopeType = Objects.requireNonNull(scopeType, "scopeType must not be null");
        this.scopeId = scopeId;
        this.scopeName = Objects.requireNonNull(scopeName, "scopeName must not be null");
        this.periodStart = Objects.requireNonNull(periodStart, "periodStart must not be null");
        this.periodEnd = Objects.requireNonNull(periodEnd, "periodEnd must not be null");
        this.baselineValue = Objects.requireNonNull(baselineValue, "baselineValue must not be null");
        this.measurementUnit = Objects.requireNonNull(measurementUnit, "measurementUnit must not be null");
        this.dataCompletenessPct = dataCompletenessPct != null ? dataCompletenessPct : new BigDecimal("100.00");
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.baselineQuality = baselineQuality != null ? baselineQuality : BaselineQuality.VERIFIED_BASELINE;
        this.calculationMethod = calculationMethod != null ? calculationMethod : "HISTORICAL_AVERAGE";
        this.seasonContext = seasonContext != null ? seasonContext : "MAIN_IRRIGATION_SEASON";
        this.approvalStatus = approvalStatus != null ? approvalStatus : "APPROVED";
        this.approvedBy = approvedBy;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
