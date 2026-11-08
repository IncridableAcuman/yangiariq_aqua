package com.aquaai.performance.api.dto;

import com.aquaai.performance.application.dto.PerformanceOverviewDto;
import com.aquaai.performance.domain.model.ImpactAssessment;
import com.aquaai.performance.domain.model.PerformanceEvaluation;
import com.aquaai.performance.domain.model.PerformanceIndicator;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class PerformanceReportResponse {
    private final String reportTitle;
    private final String reportId;
    private final Instant generatedAt;
    private final String generatedBy;
    private final Instant reportingPeriodStart;
    private final Instant reportingPeriodEnd;
    private final String geographicScope;
    private final String legalDisclaimer;
    private final PerformanceOverviewDto overview;
    private final List<PerformanceEvaluation> evaluations;
    private final List<ImpactAssessment> impactAssessments;
    private final List<PerformanceIndicator> indicators;
}
