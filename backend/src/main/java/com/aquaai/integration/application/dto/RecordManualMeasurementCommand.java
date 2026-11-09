package com.aquaai.integration.application.dto;

import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordManualMeasurementCommand(
        UUID fieldId,
        UUID sensorId,
        SensorType sensorType,
        BigDecimal value,
        String unit,
        Instant measuredAt,
        boolean verified,
        String notes
) {}
