package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.Sensor;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link Sensor} aggregates and {@link SensorDeviceJpaEntity} persistence records.
 */
@Component
public class SensorDeviceJpaMapper {

    public Sensor toDomain(SensorDeviceJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Sensor(
                entity.getId(),
                entity.getFieldId(),
                entity.getName(),
                entity.getDeviceIdentifier(),
                entity.getType(),
                entity.getStatus(),
                entity.getProvider(),
                entity.getDescription(),
                entity.getInstalledAt(),
                entity.getLastSeenAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    public SensorDeviceJpaEntity toEntity(Sensor domain) {
        if (domain == null) {
            return null;
        }
        SensorDeviceJpaEntity entity = new SensorDeviceJpaEntity();
        if (domain.getVersion() != null) {
            entity.setId(domain.getId());
            entity.setVersion(domain.getVersion());
        }
        entity.setFieldId(domain.getFieldId());
        entity.setName(domain.getName());
        entity.setDeviceIdentifier(domain.getDeviceIdentifier());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        entity.setProvider(domain.getProvider());
        entity.setDescription(domain.getDescription());
        entity.setInstalledAt(domain.getInstalledAt());
        entity.setLastSeenAt(domain.getLastSeenAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public void updateEntityFromDomain(Sensor domain, SensorDeviceJpaEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        entity.setName(domain.getName());
        entity.setStatus(domain.getStatus());
        entity.setProvider(domain.getProvider());
        entity.setDescription(domain.getDescription());
        entity.setLastSeenAt(domain.getLastSeenAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }
}
