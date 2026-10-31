package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterAllocation;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link WaterAllocation} aggregates and {@link WaterAllocationJpaEntity} persistence records.
 */
@Component
public class WaterAllocationJpaMapper {

    public WaterAllocation toDomain(WaterAllocationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WaterAllocation(
                entity.getId(),
                entity.getFieldId(),
                entity.getWaterSourceId(),
                entity.getAllocatedVolumeM3(),
                entity.getPeriodStart(),
                entity.getPeriodEnd(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public WaterAllocationJpaEntity toEntity(WaterAllocation domain) {
        if (domain == null) {
            return null;
        }
        WaterAllocationJpaEntity entity = new WaterAllocationJpaEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setFieldId(domain.getFieldId());
        entity.setWaterSourceId(domain.getWaterSourceId());
        entity.setAllocatedVolumeM3(domain.getAllocatedVolumeM3());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public void updateEntityFromDomain(WaterAllocation domain, WaterAllocationJpaEntity entity) {
        entity.setAllocatedVolumeM3(domain.getAllocatedVolumeM3());
        entity.setPeriodStart(domain.getPeriodStart());
        entity.setPeriodEnd(domain.getPeriodEnd());
        entity.setStatus(domain.getStatus());
        entity.setNotes(domain.getNotes());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
