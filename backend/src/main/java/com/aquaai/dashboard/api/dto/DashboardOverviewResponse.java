package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class DashboardOverviewResponse {
    private final FieldMetricsDto fieldMetrics;
    private final WaterMetricsDto waterMetrics;
    private final SensorMetricsDto sensorMetrics;
    private final AnomalyMetricsDto anomalyMetrics;
    private final RecommendationMetricsDto recommendationMetrics;
    private final IrrigationMetricsDto irrigationMetrics;
    private final WaterBudgetSummaryDto waterBudget;
    private final List<PriorityFieldDto> priorityFields;
    private final List<UrgentActionDto> urgentActions;
    private final List<RecentActivityDto> recentActivity;
    private final Instant generatedAt;
    private final String tenantScope;
}
