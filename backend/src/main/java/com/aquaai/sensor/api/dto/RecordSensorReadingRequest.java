package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.model.MeasurementUnit;
import com.aquaai.sensor.domain.model.ReadingQuality;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Request payload for submitting a sensor telemetry observation")
public record RecordSensorReadingRequest(
        @NotNull(message = "Measurement value is required")
        @Schema(description = "Numeric measurement value", example = "58.45")
        BigDecimal value,

        @Schema(description = "Measurement unit (optional, defaults to sensor type default)", example = "PERCENT")
        MeasurementUnit unit,

        @Schema(description = "Observation quality flag (defaults to VALID)", example = "VALID")
        ReadingQuality quality,

        @Size(max = 64, message = "Source tag cannot exceed 64 characters")
        @Schema(description = "Source origin tag", example = "INGEST_API")
        String source,

        @Schema(description = "Observation timestamp (defaults to current instant)", example = "2026-11-01T07:30:00Z")
        Instant measuredAt
) {
}
