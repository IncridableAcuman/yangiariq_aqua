package com.aquaai.performance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain aggregate representing a registered operational performance indicator.
 */
@Getter
@Builder(toBuilder = true)
public class PerformanceIndicator {

    private final UUID id;
    private final String code;
    private final String name;
    private final String description;
    private final IndicatorCategory category;
    private final MeasurementUnit measurementUnit;
    private final String calculationMethod;
    private final AggregationMethod aggregationMethod;
    private final GeographicScopeType supportedGeographicScope;
    private final String evidenceRequirement;
    private final BigDecimal minimumDataQualityPct;
    private final String applicableTimePeriod;
    private final String sourceModule;
    private final String calculationVersion;
    private final Instant createdAt;
    private final Instant updatedAt;

    public PerformanceIndicator(
            UUID id,
            String code,
            String name,
            String description,
            IndicatorCategory category,
            MeasurementUnit measurementUnit,
            String calculationMethod,
            AggregationMethod aggregationMethod,
            GeographicScopeType supportedGeographicScope,
            String evidenceRequirement,
            BigDecimal minimumDataQualityPct,
            String applicableTimePeriod,
            String sourceModule,
            String calculationVersion,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.code = Objects.requireNonNull(code, "code must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.measurementUnit = Objects.requireNonNull(measurementUnit, "measurementUnit must not be null");
        this.calculationMethod = Objects.requireNonNull(calculationMethod, "calculationMethod must not be null");
        this.aggregationMethod = Objects.requireNonNull(aggregationMethod, "aggregationMethod must not be null");
        this.supportedGeographicScope = Objects.requireNonNull(supportedGeographicScope, "supportedGeographicScope must not be null");
        this.evidenceRequirement = evidenceRequirement != null ? evidenceRequirement : "OBSERVED_OR_CALCULATED";
        this.minimumDataQualityPct = minimumDataQualityPct != null ? minimumDataQualityPct : new BigDecimal("80.00");
        this.applicableTimePeriod = applicableTimePeriod != null ? applicableTimePeriod : "MONTHLY";
        this.sourceModule = Objects.requireNonNull(sourceModule, "sourceModule must not be null");
        this.calculationVersion = calculationVersion != null ? calculationVersion : "v1.0.0";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }
}
