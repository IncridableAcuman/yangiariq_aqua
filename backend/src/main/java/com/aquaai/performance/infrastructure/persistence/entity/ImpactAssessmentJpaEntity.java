package com.aquaai.performance.infrastructure.persistence.entity;

import com.aquaai.performance.domain.model.AssessmentReviewStatus;
import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.ChangeDirection;
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
@Table(name = "impact_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImpactAssessmentJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "assessment_number", nullable = false, unique = true, length = 32)
    private String assessmentNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "intervention_type", nullable = false, length = 64)
    private String interventionType;

    @Column(name = "intervention_id", nullable = false)
    private UUID interventionId;

    @Column(name = "intervention_reference", nullable = false, length = 255)
    private String interventionReference;

    @Column(name = "intervention_date", nullable = false)
    private Instant interventionDate;

    @Column(name = "indicator_code", nullable = false, length = 64)
    private String indicatorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 64)
    private GeographicScopeType scopeType;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "scope_name", nullable = false, length = 255)
    private String scopeName;

    @Column(name = "baseline_id")
    private UUID baselineId;

    @Column(name = "baseline_period_start", nullable = false)
    private Instant baselinePeriodStart;

    @Column(name = "baseline_period_end", nullable = false)
    private Instant baselinePeriodEnd;

    @Column(name = "baseline_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal baselineValue;

    @Column(name = "evaluation_period_start", nullable = false)
    private Instant evaluationPeriodStart;

    @Column(name = "evaluation_period_end", nullable = false)
    private Instant evaluationPeriodEnd;

    @Column(name = "post_intervention_value", precision = 18, scale = 4, nullable = false)
    private BigDecimal postInterventionValue;

    @Column(name = "absolute_change", precision = 18, scale = 4, nullable = false)
    private BigDecimal absoluteChange;

    @Column(name = "percentage_change", precision = 8, scale = 2, nullable = false)
    private BigDecimal percentageChange;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_direction", nullable = false, length = 32)
    private ChangeDirection changeDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 32)
    private MeasurementUnit measurementUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_classification", nullable = false, length = 32)
    private EvidenceClassification evidenceClassification;

    @Enumerated(EnumType.STRING)
    @Column(name = "baseline_quality", nullable = false, length = 32)
    private BaselineQuality baselineQuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "attribution_level", nullable = false, length = 32)
    private AttributionLevel attributionLevel;

    @Column(name = "confounding_factors", length = 2000)
    private String confoundingFactors;

    @Column(name = "data_limitations", length = 2000)
    private String dataLimitations;

    @Column(name = "comparison_valid", nullable = false)
    private boolean comparisonValid;

    @Column(name = "methodology_notes", nullable = false, length = 2000)
    private String methodologyNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false, length = 32)
    private AssessmentReviewStatus reviewStatus;

    @Column(name = "reviewer_id")
    private UUID reviewerId;

    @Column(name = "reviewer_name", length = 255)
    private String reviewerName;

    @Column(name = "review_notes", length = 2000)
    private String reviewNotes;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
