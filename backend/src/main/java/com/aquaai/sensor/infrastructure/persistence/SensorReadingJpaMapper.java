package com.aquaai.sensor.infrastructure.persistence;

import com.aquaai.sensor.domain.model.SensorReading;
import org.springframework.stereotype.Component;

/**
 * Maps between pure domain {@link SensorReading} values and {@link SensorReadingJpaEntity} persistence records.
 */
@Component
public class SensorReadingJpaMapper {

    public SensorReading toDomain(SensorReadingJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SensorReading(
                entity.getId(),
                entity.getSensorId(),
                entity.getFieldId(),
                entity.getType(),
                entity.getValue(),
                entity.getUnit(),
                entity.getQuality(),
                entity.getSource(),
                entity.getMeasuredAt(),
                entity.getCreatedAt()
        );
    }

    public SensorReadingJpaEntity toEntity(SensorReading domain) {
        if (domain == null) {
            return null;
        }
        SensorReadingJpaEntity entity = new SensorReadingJpaEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setSensorId(domain.getSensorId());
        entity.setFieldId(domain.getFieldId());
        entity.setType(domain.getType());
        entity.setValue(domain.getValue());
        entity.setUnit(domain.getUnit());
        entity.setQuality(domain.getQuality());
        entity.setSource(domain.getSource());
        entity.setMeasuredAt(domain.getMeasuredAt());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
