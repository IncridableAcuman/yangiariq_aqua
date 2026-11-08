package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain model representing an operational or policy performance target.
 */
@Getter
@Builder(toBuilder = true)
public class PerformanceTarget {

    private final UUID id;
    private final String indicatorCode;
    private final String title;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final TargetType targetType;
    private final TargetCategory targetCategory;
    private final BigDecimal targetValue;
    private final BigDecimal rangeMin;
    private final BigDecimal rangeMax;
    private final MeasurementUnit measurementUnit;
    private final Instant effectiveStart;
    private final Instant effectiveEnd;
    private final String targetOwner;
    private final TargetStatus status;
    private final String rationale;
    private final String targetVersion;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PerformanceTarget(
            UUID id,
            String indicatorCode,
            String title,
            GeographicScopeType scopeType,
            UUID scopeId,
            String scopeName,
            TargetType targetType,
            TargetCategory targetCategory,
            BigDecimal targetValue,
            BigDecimal rangeMin,
            BigDecimal rangeMax,
            MeasurementUnit measurementUnit,
            Instant effectiveStart,
            Instant effectiveEnd,
            String targetOwner,
            TargetStatus status,
            String rationale,
            String targetVersion,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.indicatorCode = Objects.requireNonNull(indicatorCode, "indicatorCode must not be null");
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.scopeType = Objects.requireNonNull(scopeType, "scopeType must not be null");
        this.scopeId = scopeId;
        this.scopeName = Objects.requireNonNull(scopeName, "scopeName must not be null");
        this.targetType = Objects.requireNonNull(targetType, "targetType must not be null");
        this.targetCategory = targetCategory != null ? targetCategory : TargetCategory.OPERATIONAL_TARGET;
        this.targetValue = Objects.requireNonNull(targetValue, "targetValue must not be null");
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.measurementUnit = Objects.requireNonNull(measurementUnit, "measurementUnit must not be null");
        this.effectiveStart = Objects.requireNonNull(effectiveStart, "effectiveStart must not be null");
        this.effectiveEnd = effectiveEnd;
        this.targetOwner = Objects.requireNonNull(targetOwner, "targetOwner must not be null");
        this.status = status != null ? status : TargetStatus.APPROVED;
        this.rationale = rationale;
        this.targetVersion = targetVersion != null ? targetVersion : "v1.0.0";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
