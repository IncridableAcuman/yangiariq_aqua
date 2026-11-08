package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.AggregationMethod;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.IndicatorCategory;
import com.aquaai.performance.domain.model.MeasurementUnit;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PerformanceIndicatorResponse {
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
}
