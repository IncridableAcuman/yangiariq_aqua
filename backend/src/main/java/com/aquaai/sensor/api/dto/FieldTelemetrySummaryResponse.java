package com.aquaai.sensor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Aggregated latest telemetry observation for each sensor type deployed on a field")
public record FieldTelemetrySummaryResponse(
        UUID fieldId,
        List<SensorReadingResponse> readings,
        Instant generatedAt
) {
}
