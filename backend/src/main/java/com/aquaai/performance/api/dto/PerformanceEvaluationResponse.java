package com.aquaai.performance.api.dto;

import com.aquaai.performance.domain.model.EvidenceClassification;
import com.aquaai.performance.domain.model.GeographicScopeType;
import com.aquaai.performance.domain.model.MeasurementUnit;
import com.aquaai.performance.domain.model.PerformanceTrend;
import com.aquaai.performance.domain.model.TargetComplianceStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class PerformanceEvaluationResponse {
    private final UUID id;
    private final String evaluationNumber;
    private final String indicatorCode;
    private final GeographicScopeType scopeType;
    private final UUID scopeId;
    private final String scopeName;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal calculatedValue;
    private final MeasurementUnit measurementUnit;
    private final UUID baselineId;
    private final BigDecimal baselineValue;
    private final UUID targetId;
    private final BigDecimal targetValue;
    private final TargetComplianceStatus targetCompliance;
    private final BigDecimal varianceFromBaselinePct;
    private final BigDecimal varianceFromTargetPct;
    private final PerformanceTrend trend;
    private final EvidenceClassification evidenceClassification;
    private final BigDecimal observedSampleRatio;
    private final int missingDataIntervals;
    private final boolean comparisonValid;
    private final String validityNotes;
    private final String explanatorySummary;
    private final String calculationVersion;
    private final Instant evaluatedAt;
}
