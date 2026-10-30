package com.aquaai.sensor.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record SensorReading(
        UUID sensorId,
        UUID fieldId,
        SensorType sensorType,
        BigDecimal value,
        String unit,
        BigDecimal batteryLevelPercent,
        Instant measuredAt,
        String providerSource
) {
}
