package com.aquaai.analytics.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class AnalyticsOverviewResponse {
    private final PerformancePeriodDto period;
    private final FarmOverviewMetricsDto farmOverview;
    private final WaterPerformanceDto waterPerformance;
    private final IrrigationPerformanceDto irrigationPerformance;
    private final TelemetryQualityDto telemetryQuality;
    private final VerificationPerformanceDto verificationPerformance;
    private final SafetyEventMetricsDto safetyMetrics;
    private final PeriodComparisonDto periodComparison;
    private final DataConfidenceDto dataConfidence;
    private final List<FarmInsightDto> topInsights;
    private final Instant generatedAt;
    private final String tenantScope;
}
