package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationEvent;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link IrrigationEvent} aggregates and {@link IrrigationEventJpaEntity} persistence records.
 */
@Component
public class IrrigationEventJpaMapper {

    public IrrigationEvent toDomain(IrrigationEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new IrrigationEvent(
                entity.getId(),
                entity.getFieldId(),
                entity.getIrrigationPlanId(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getActualVolumeM3(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public IrrigationEventJpaEntity toEntity(IrrigationEvent domain) {
        if (domain == null) {
            return null;
        }
        IrrigationEventJpaEntity entity = new IrrigationEventJpaEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setFieldId(domain.getFieldId());
        entity.setIrrigationPlanId(domain.getIrrigationPlanId());
        entity.setStartedAt(domain.getStartedAt());
        entity.setEndedAt(domain.getEndedAt());
        entity.setActualVolumeM3(domain.getActualVolumeM3());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public void updateEntityFromDomain(IrrigationEvent domain, IrrigationEventJpaEntity entity) {
        entity.setEndedAt(domain.getEndedAt());
        entity.setActualVolumeM3(domain.getActualVolumeM3());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
