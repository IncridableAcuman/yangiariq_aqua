package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImpactAssessmentRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Intervention type is required")
    private String interventionType;

    @NotNull(message = "Intervention ID is required")
    private UUID interventionId;

    @NotBlank(message = "Intervention reference is required")
    private String interventionReference;

    @NotNull(message = "Intervention date is required")
    private Instant interventionDate;

    @NotBlank(message = "Indicator code is required")
    private String indicatorCode;

    @NotNull(message = "Scope type is required")
    private GeographicScopeType scopeType;

    private UUID scopeId;

    @NotBlank(message = "Scope name is required")
    private String scopeName;

    private UUID baselineId;

    @NotNull(message = "Baseline period start is required")
    private Instant baselinePeriodStart;

    @NotNull(message = "Baseline period end is required")
    private Instant baselinePeriodEnd;

    @NotNull(message = "Baseline value is required")
    private BigDecimal baselineValue;

    @NotNull(message = "Evaluation period start is required")
    private Instant evaluationPeriodStart;

    @NotNull(message = "Evaluation period end is required")
    private Instant evaluationPeriodEnd;

    @NotNull(message = "Post-intervention value is required")
    private BigDecimal postInterventionValue;

    @NotNull(message = "Measurement unit is required")
    private MeasurementUnit measurementUnit;

    private EvidenceClassification evidenceClassification;
    private BaselineQuality baselineQuality;
    private AttributionLevel attributionLevel;
    private String confoundingFactors;
    private String dataLimitations;

    @NotBlank(message = "Methodology notes are required")
    private String methodologyNotes;
}
