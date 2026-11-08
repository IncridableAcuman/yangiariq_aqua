package com.aquaai.performance.application.dto;

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
public class CreateBaselineCommand {
    private final String indicatorCode;
    private final String name;
    private final String description;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal baselineValue;
    private final MeasurementUnit measurementUnit;
    private final BigDecimal dataCompletenessPct;
    private final EvidenceClassification evidenceClassification;
    private final BaselineQuality baselineQuality;
    private final String calculationMethod;
    private final String seasonContext;
    private final UUID approvedBy;
}
