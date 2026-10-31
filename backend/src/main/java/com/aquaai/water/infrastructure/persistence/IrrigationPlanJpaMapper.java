package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationPlan;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link IrrigationPlan} aggregates and {@link IrrigationPlanJpaEntity} persistence records.
 */
@Component
public class IrrigationPlanJpaMapper {

    public IrrigationPlan toDomain(IrrigationPlanJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new IrrigationPlan(
                entity.getId(),
                entity.getFieldId(),
                entity.getWaterAllocationId(),
                entity.getPlannedStartAt(),
                entity.getPlannedEndAt(),
                entity.getPlannedVolumeM3(),
                entity.getMethod(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public IrrigationPlanJpaEntity toEntity(IrrigationPlan domain) {
        if (domain == null) {
            return null;
        }
        IrrigationPlanJpaEntity entity = new IrrigationPlanJpaEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setFieldId(domain.getFieldId());
        entity.setWaterAllocationId(domain.getWaterAllocationId());
        entity.setPlannedStartAt(domain.getPlannedStartAt());
        entity.setPlannedEndAt(domain.getPlannedEndAt());
        entity.setPlannedVolumeM3(domain.getPlannedVolumeM3());
        entity.setMethod(domain.getMethod());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public void updateEntityFromDomain(IrrigationPlan domain, IrrigationPlanJpaEntity entity) {
        entity.setWaterAllocationId(domain.getWaterAllocationId());
        entity.setPlannedStartAt(domain.getPlannedStartAt());
        entity.setPlannedEndAt(domain.getPlannedEndAt());
        entity.setPlannedVolumeM3(domain.getPlannedVolumeM3());
        entity.setMethod(domain.getMethod());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
