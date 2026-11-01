package com.aquaai.irrigation.api.dto;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class SmartIrrigationPlanResponse {

    private final UUID id;
    private final UUID fieldId;
    private final UUID waterAllocationId;
    private final IrrigationPlanStatus status;
    private final IrrigationPriority priority;
    private final LocalDate plannedDate;
    private final Instant plannedStartAt;
    private final Instant plannedEndAt;
    private final BigDecimal recommendedVolumeM3;
    private final int estimatedDurationMinutes;
    private final BigDecimal recommendedFlowRateM3H;
    private final BigDecimal targetMoisturePercent;
    private final BigDecimal currentMoisturePercent;
    private final String confidenceLevel;
    private final BigDecimal confidenceScore;
    private final String reason;
    private final String reasoningSummary;
    private final String cancellationReason;
    private final boolean isBlocked;
    private final String blockedReason;
    private final UUID approvedBy;
    private final Instant approvedAt;
    private final String source;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Long version;
    private final List<PlanConstraintResponse> constraints;
    private final List<PlanTradeoffResponse> tradeoffs;

    public static SmartIrrigationPlanResponse fromDomain(SmartIrrigationPlan plan) {
        if (plan == null) {
            return null;
        }

        List<PlanConstraintResponse> constraintResponses = plan.getConstraints() != null
                ? plan.getConstraints().stream().map(PlanConstraintResponse::fromDomain).collect(Collectors.toList())
                : List.of();

        List<PlanTradeoffResponse> tradeoffResponses = plan.getTradeoffs() != null
                ? plan.getTradeoffs().stream().map(PlanTradeoffResponse::fromDomain).collect(Collectors.toList())
                : List.of();

        return SmartIrrigationPlanResponse.builder()
                .id(plan.getId())
                .fieldId(plan.getFieldId())
                .waterAllocationId(plan.getWaterAllocationId())
                .status(plan.getStatus())
                .priority(plan.getPriority())
                .plannedDate(plan.getPlannedDate())
                .plannedStartAt(plan.getPlannedStartAt())
                .plannedEndAt(plan.getPlannedEndAt())
                .recommendedVolumeM3(plan.getRecommendedVolumeM3())
                .estimatedDurationMinutes(plan.getEstimatedDurationMinutes())
                .recommendedFlowRateM3H(plan.getRecommendedFlowRateM3H())
                .targetMoisturePercent(plan.getTargetMoisturePercent())
                .currentMoisturePercent(plan.getCurrentMoisturePercent())
                .confidenceLevel(plan.getConfidenceLevel())
                .confidenceScore(plan.getConfidenceScore())
                .reason(plan.getReason())
                .reasoningSummary(plan.getReasoningSummary())
                .cancellationReason(plan.getCancellationReason())
                .isBlocked(plan.isBlocked())
                .blockedReason(plan.getBlockedReason())
                .approvedBy(plan.getApprovedBy())
                .approvedAt(plan.getApprovedAt())
                .source(plan.getSource())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .version(plan.getVersion())
                .constraints(constraintResponses)
                .tradeoffs(tradeoffResponses)
                .build();
    }
}
