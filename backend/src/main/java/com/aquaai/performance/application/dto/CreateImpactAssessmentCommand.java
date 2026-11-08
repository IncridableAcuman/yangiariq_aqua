package com.aquaai.performance.application.dto;

import com.aquaai.performance.domain.model.AttributionLevel;
import com.aquaai.performance.domain.model.BaselineQuality;
import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CreateImpactAssessmentCommand {
    private final String title;
    private final String interventionType;
    private final UUID interventionId;
    private final String interventionReference;
    private final Instant interventionDate;
    private final String indicatorCode;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final UUID baselineId;
    private final Instant baselinePeriodStart;
    private final Instant baselinePeriodEnd;
    private final BigDecimal baselineValue;
    private final Instant evaluationPeriodStart;
    private final Instant evaluationPeriodEnd;
    private final BigDecimal postInterventionValue;
    private final MeasurementUnit measurementUnit;
    private final EvidenceClassification evidenceClassification;
    private final BaselineQuality baselineQuality;
    private final AttributionLevel attributionLevel;
    private final String confoundingFactors;
    private final String dataLimitations;
    private final String methodologyNotes;
}
