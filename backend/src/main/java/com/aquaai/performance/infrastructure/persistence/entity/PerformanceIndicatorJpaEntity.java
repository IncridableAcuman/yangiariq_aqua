package com.aquaai.performance.infrastructure.persistence.entity;

import com.aquaai.performance.domain.model.AggregationMethod;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.IndicatorCategory;
import com.aquaai.performance.domain.model.MeasurementUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "performance_indicators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceIndicatorJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 64)
    private IndicatorCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 32)
    private MeasurementUnit measurementUnit;

    @Column(name = "calculation_method", nullable = false, length = 1000)
    private String calculationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_method", nullable = false, length = 32)
    private AggregationMethod aggregationMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "supported_geographic_scope", nullable = false, length = 64)
    private GeographicScopeType supportedGeographicScope;

    @Column(name = "evidence_requirement", nullable = false, length = 64)
    private String evidenceRequirement;

    @Column(name = "minimum_data_quality_pct", precision = 5, scale = 2, nullable = false)
    private BigDecimal minimumDataQualityPct;

    @Column(name = "applicable_time_period", nullable = false, length = 32)
    private String applicableTimePeriod;

    @Column(name = "source_module", nullable = false, length = 64)
    private String sourceModule;

    @Column(name = "calculation_version", nullable = false, length = 32)
    private String calculationVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
