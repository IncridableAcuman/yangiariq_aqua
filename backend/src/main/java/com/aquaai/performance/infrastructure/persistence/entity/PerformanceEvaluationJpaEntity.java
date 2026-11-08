package com.aquaai.performance.infrastructure.persistence.entity;

import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.PerformanceTrend;
import com.aquaai.performance.domain.model.TargetComplianceStatus;
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
@Table(name = "performance_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceEvaluationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "evaluation_number", nullable = false, unique = true, length = 32)
    private String evaluationNumber;

    @Column(name = "indicator_code", nullable = false, length = 64)
    private String indicatorCode;

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

    @Column(name = "calculated_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal calculatedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 32)
    private MeasurementUnit measurementUnit;

    @Column(name = "baseline_id")
    private UUID baselineId;

    @Column(name = "baseline_value", precision = 18, scale = 4)
    private BigDecimal baselineValue;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "target_value", precision = 18, scale = 4)
    private BigDecimal targetValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_compliance", nullable = false, length = 32)
    private TargetComplianceStatus targetCompliance;

    @Column(name = "variance_from_baseline_pct", precision = 8, scale = 2)
    private BigDecimal varianceFromBaselinePct;

    @Column(name = "variance_from_target_pct", precision = 8, scale = 2)
    private BigDecimal varianceFromTargetPct;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend", nullable = false, length = 32)
    private PerformanceTrend trend;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_classification", nullable = false, length = 32)
    private EvidenceClassification evidenceClassification;

    @Column(name = "observed_sample_ratio", precision = 5, scale = 2, nullable = false)
    private BigDecimal observedSampleRatio;

    @Column(name = "missing_data_intervals", nullable = false)
    private int missingDataIntervals;

    @Column(name = "comparison_valid", nullable = false)
    private boolean comparisonValid;

    @Column(name = "validity_notes", length = 1000)
    private String validityNotes;

    @Column(name = "explanatory_summary", nullable = false, length = 2000)
    private String explanatorySummary;

    @Column(name = "calculation_version", nullable = false, length = 32)
    private String calculationVersion;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;
}
