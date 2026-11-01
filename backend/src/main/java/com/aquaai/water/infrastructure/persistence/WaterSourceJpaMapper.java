package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.WaterSource;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link WaterSource} aggregates and {@link WaterSourceJpaEntity} persistence records.
 */
@Component
public class WaterSourceJpaMapper {

    public WaterSource toDomain(WaterSourceJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new WaterSource(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public WaterSourceJpaEntity toEntity(WaterSource domain) {
        if (domain == null) {
            return null;
        }
        WaterSourceJpaEntity entity = new WaterSourceJpaEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public void updateEntityFromDomain(WaterSource domain, WaterSourceJpaEntity entity) {
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setDescription(domain.getDescription());
        entity.setStatus(domain.getStatus());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
