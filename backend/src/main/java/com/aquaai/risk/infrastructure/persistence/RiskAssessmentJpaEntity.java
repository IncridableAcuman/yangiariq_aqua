package com.aquaai.risk.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "farm_risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    @Column(name = "field_name", nullable = false)
    private String fieldName;

    @Column(name = "overall_severity", nullable = false, length = 32)
    private String overallSeverity;

    @Column(name = "overall_confidence", nullable = false, length = 32)
    private String overallConfidence;

    @Column(name = "risk_trend", nullable = false, length = 32)
    private String riskTrend;

    @Column(name = "risk_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal riskScore;

    @Column(name = "is_hydraulic_blocked", nullable = false)
    private boolean isHydraulicBlocked;

    @Column(name = "hydraulic_block_reason", length = 500)
    private String hydraulicBlockReason;

    @Column(name = "primary_risk_category", nullable = false, length = 64)
    private String primaryRiskCategory;

    @Column(name = "lifecycle_status", nullable = false, length = 32)
    private String lifecycleStatus;

    @Column(name = "priority_rank", nullable = false)
    private int priorityRank;

    @Column(name = "priority_reason", nullable = false, length = 500)
    private String priorityReason;

    @Column(name = "risk_factors_json", nullable = false, columnDefinition = "TEXT")
    private String riskFactorsJson;

    @Column(name = "recommendations_json", nullable = false, columnDefinition = "TEXT")
    private String recommendationsJson;

    @Column(name = "missing_data_warnings_json", columnDefinition = "TEXT")
    private String missingDataWarningsJson;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "acknowledged_by")
    private UUID acknowledgedBy;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @Column(name = "resolution_notes", length = 1000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
