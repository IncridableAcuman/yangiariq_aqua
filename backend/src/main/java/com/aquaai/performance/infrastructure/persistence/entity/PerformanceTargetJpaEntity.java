package com.aquaai.performance.infrastructure.persistence.entity;

import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.TargetCategory;
import com.aquaai.performance.domain.model.TargetStatus;
import com.aquaai.performance.domain.model.TargetType;
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
@Table(name = "performance_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceTargetJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "indicator_code", nullable = false, length = 64)
    private String indicatorCode;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 64)
    private GeographicScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "scope_name", nullable = false, length = 255)
    private String scopeName;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_category", nullable = false, length = 32)
    private TargetCategory targetCategory;

    @Column(name = "target_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal targetValue;

    @Column(name = "range_min", precision = 18, scale = 4)
    private BigDecimal rangeMin;

    @Column(name = "range_max", precision = 18, scale = 4)
    private BigDecimal rangeMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 32)
    private MeasurementUnit measurementUnit;

    @Column(name = "effective_start", nullable = false)
    private Instant effectiveStart;

    @Column(name = "effective_end")
    private Instant effectiveEnd;

    @Column(name = "target_owner", nullable = false, length = 255)
    private String targetOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TargetStatus status;

    @Column(name = "rationale", length = 2000)
    private String rationale;

    @Column(name = "target_version", nullable = false, length = 32)
    private String targetVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
