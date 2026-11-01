package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.sensor.domain.model.SensorType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Response representation of a field sensor device")
public record SensorResponse(
        UUID id,
        UUID fieldId,
        String name,
        String deviceIdentifier,
        SensorType type,
        SensorStatus status,
        String provider,
        String description,
        Instant installedAt,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {
    public static SensorResponse from(Sensor sensor) {
        if (sensor == null) {
            return null;
        }
        return new SensorResponse(
                sensor.getId(),
                sensor.getFieldId(),
                sensor.getName(),
                sensor.getDeviceIdentifier(),
                sensor.getType(),
                sensor.getStatus(),
                sensor.getProvider(),
                sensor.getDescription(),
                sensor.getInstalledAt(),
                sensor.getLastSeenAt(),
                sensor.getCreatedAt(),
                sensor.getUpdatedAt(),
                sensor.getVersion()
        );
    }
}
