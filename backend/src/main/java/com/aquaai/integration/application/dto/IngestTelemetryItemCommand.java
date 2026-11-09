package com.aquaai.integration.application.dto;

import com.aquaai.sensor.domain.model.SensorType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record IngestTelemetryItemCommand(
        String externalRecordId,
        UUID fieldId,
        UUID sensorId,
        String deviceIdentifier,
        SensorType sensorType,
        BigDecimal value,
        String unit,
        Instant measuredAt,
        String evidenceClassification
) {}
