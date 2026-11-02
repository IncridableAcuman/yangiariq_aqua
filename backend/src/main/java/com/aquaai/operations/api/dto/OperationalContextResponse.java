package com.aquaai.operations.api.dto;

import com.aquaai.irrigation.api.dto.SmartIrrigationPlanResponse;
import com.aquaai.operations.domain.model.OperationalWorkflowState;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Unified Operational Context aggregation model providing single-pass intelligence
 * for the Decision-to-Action Workspace.
 */
@Value
@Builder
public class OperationalContextResponse {

    FieldSummaryDto field;
    SituationSummaryDto situation;
    AnomalySummaryDto activeAnomaly;
    RecommendationSummaryDto activeRecommendation;
    SmartIrrigationPlanResponse activePlan;
    IrrigationEventSummaryDto activeIrrigationEvent;
    WorkflowSummaryDto workflow;

    @Value
    @Builder
    public static class FieldSummaryDto {
        UUID id;
        String name;
        String cropType;
        BigDecimal areaHectares;
        BigDecimal latitude;
        BigDecimal longitude;
        String status;
    }

    @Value
    @Builder
    public static class SituationSummaryDto {
        BigDecimal latestSoilMoisture;
        BigDecimal soilMoistureTrend12h;
        BigDecimal temperatureCelsius;
        String telemetryFreshness; // FRESH, STALE, MISSING
        Instant lastTelemetryAt;
        BigDecimal waterRemainingM3;
        BigDecimal waterAllocatedM3;
        BigDecimal waterConsumedM3;
        String hydraulicSafetyStatus; // SAFE, WARNING, BLOCKED
        String hydraulicBlockedReason;
    }

    @Value
    @Builder
    public static class AnomalySummaryDto {
        UUID id;
        String type;
        String severity;
        String description;
        Instant detectedAt;
        boolean isBlocking;
    }

    @Value
    @Builder
    public static class RecommendationSummaryDto {
        UUID id;
        String type;
        String priority;
        String reason;
        String confidence;
        BigDecimal suggestedVolumeM3;
        Integer suggestedDurationMinutes;
        String aiExplanation;
        List<String> evidence;
    }

    @Value
    @Builder
    public static class IrrigationEventSummaryDto {
        UUID id;
        String status;
        Instant startedAt;
        Instant endedAt;
        BigDecimal actualVolumeM3;
        String notes;
    }

    @Value
    @Builder
    public static class WorkflowSummaryDto {
        OperationalWorkflowState currentState;
        boolean isBlocked;
        String blockedReason;
        List<WorkflowStepDto> timeline;
        List<String> availableActions;
        ScheduleConflictDto scheduleConflict;
    }
}
