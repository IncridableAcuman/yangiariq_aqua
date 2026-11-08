package com.aquaai.performance.infrastructure.persistence.entity;

import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
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
@Table(name = "performance_baselines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceBaselineJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "indicator_code", nullable = false, length = 64)
    private String indicatorCode;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 64)
    private GeographicScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "scope_name", nullable = false, length = 255)
    private String scopeName;

    @Column(name = "period_start", nullable = false)
    private Instant periodStart;

    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;

    @Column(name = "baseline_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal baselineValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 32)
    private MeasurementUnit measurementUnit;

    @Column(name = "data_completeness_pct", precision = 5, scale = 2, nullable = false)
    private BigDecimal dataCompletenessPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_classification", nullable = false, length = 32)
    private EvidenceClassification evidenceClassification;

    @Enumerated(EnumType.STRING)
    @Column(name = "baseline_quality", nullable = false, length = 32)
    private BaselineQuality baselineQuality;

    @Column(name = "calculation_method", nullable = false, length = 500)
    private String calculationMethod;

    @Column(name = "season_context", length = 64)
    private String seasonContext;

    @Column(name = "approval_status", nullable = false, length = 32)
    private String approvalStatus;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
