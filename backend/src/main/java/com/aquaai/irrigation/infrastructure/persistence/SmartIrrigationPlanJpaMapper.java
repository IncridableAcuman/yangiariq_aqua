package com.aquaai.irrigation.infrastructure.persistence;

import com.aquaai.irrigation.domain.model.PlanConstraint;
import com.aquaai.irrigation.domain.model.PlanTradeoff;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SmartIrrigationPlanJpaMapper {

    public SmartIrrigationPlanJpaEntity toJpaEntity(SmartIrrigationPlan domain) {
        if (domain == null) {
            return null;
        }

        SmartIrrigationPlanJpaEntity entity = new SmartIrrigationPlanJpaEntity();
        entity.setId(domain.getId());
        entity.setFieldId(domain.getFieldId());
        entity.setWaterAllocationId(domain.getWaterAllocationId());
        entity.setStatus(domain.getStatus());
        entity.setPriority(domain.getPriority());
        entity.setPlannedDate(domain.getPlannedDate());
        entity.setPlannedStartAt(domain.getPlannedStartAt());
        entity.setPlannedEndAt(domain.getPlannedEndAt());
        entity.setRecommendedVolumeM3(domain.getRecommendedVolumeM3());
        entity.setEstimatedDurationMinutes(domain.getEstimatedDurationMinutes());
        entity.setRecommendedFlowRateM3H(domain.getRecommendedFlowRateM3H());
        entity.setTargetMoisturePercent(domain.getTargetMoisturePercent());
        entity.setCurrentMoisturePercent(domain.getCurrentMoisturePercent());
        entity.setConfidenceLevel(domain.getConfidenceLevel());
        entity.setConfidenceScore(domain.getConfidenceScore());
        entity.setReason(domain.getReason());
        entity.setReasoningSummary(domain.getReasoningSummary());
        entity.setCancellationReason(domain.getCancellationReason());
        entity.setBlocked(domain.isBlocked());
        entity.setBlockedReason(domain.getBlockedReason());
        entity.setApprovedBy(domain.getApprovedBy());
        entity.setApprovedAt(domain.getApprovedAt());
        entity.setSource(domain.getSource());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setVersion(domain.getVersion());

        // Map constraints
        List<PlanConstraintJpaEntity> constraintEntities = new ArrayList<>();
        if (domain.getConstraints() != null) {
            for (PlanConstraint c : domain.getConstraints()) {
                constraintEntities.add(PlanConstraintJpaEntity.builder()
                        .id(c.getId())
                        .plan(entity)
                        .type(c.getType())
                        .status(c.getStatus())
                        .message(c.getMessage())
                        .details(c.getDetails())
                        .createdAt(domain.getCreatedAt())
                        .build());
            }
        }
        entity.setConstraints(constraintEntities);

        // Map tradeoffs
        List<PlanTradeoffJpaEntity> tradeoffEntities = new ArrayList<>();
        if (domain.getTradeoffs() != null) {
            for (PlanTradeoff t : domain.getTradeoffs()) {
                tradeoffEntities.add(PlanTradeoffJpaEntity.builder()
                        .id(t.getId())
                        .plan(entity)
                        .category(t.getCategory())
                        .description(t.getDescription())
                        .createdAt(domain.getCreatedAt())
                        .build());
            }
        }
        entity.setTradeoffs(tradeoffEntities);

        return entity;
    }

    public SmartIrrigationPlan toDomain(SmartIrrigationPlanJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<PlanConstraint> constraints = new ArrayList<>();
        if (entity.getConstraints() != null) {
            for (PlanConstraintJpaEntity c : entity.getConstraints()) {
                constraints.add(PlanConstraint.builder()
                        .id(c.getId())
                        .type(c.getType())
                        .status(c.getStatus())
                        .message(c.getMessage())
                        .details(c.getDetails())
                        .build());
            }
        }

        List<PlanTradeoff> tradeoffs = new ArrayList<>();
        if (entity.getTradeoffs() != null) {
            for (PlanTradeoffJpaEntity t : entity.getTradeoffs()) {
                tradeoffs.add(PlanTradeoff.builder()
                        .id(t.getId())
                        .category(t.getCategory())
                        .description(t.getDescription())
                        .build());
            }
        }

        return new SmartIrrigationPlan(
                entity.getId(),
                entity.getFieldId(),
                entity.getWaterAllocationId(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getPlannedDate(),
                entity.getPlannedStartAt(),
                entity.getPlannedEndAt(),
                entity.getRecommendedVolumeM3(),
                entity.getEstimatedDurationMinutes(),
                entity.getRecommendedFlowRateM3H(),
                entity.getTargetMoisturePercent(),
                entity.getCurrentMoisturePercent(),
                entity.getConfidenceLevel(),
                entity.getConfidenceScore(),
                entity.getReason(),
                entity.getReasoningSummary(),
                entity.getCancellationReason(),
                entity.isBlocked(),
                entity.getBlockedReason(),
                entity.getApprovedBy(),
                entity.getApprovedAt(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                constraints,
                tradeoffs
        );
    }
}
