package com.aquaai.integration.api.dto;

import com.aquaai.integration.application.dto.IngestBatchCommand;
import com.aquaai.integration.application.dto.IngestTelemetryItemCommand;
import com.aquaai.sensor.domain.model.SensorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IngestTelemetryBatchRequest(
        @Schema(description = "Data source code (e.g. HTTP_SENSOR_GATEWAY)", example = "HTTP_SENSOR_GATEWAY")
        String sourceCode,

        @NotEmpty(message = "Items list must not be empty")
        @Valid
        @Schema(description = "Array of telemetry observations")
        List<ItemRequest> items
) {
    public record ItemRequest(
            @Schema(description = "External unique record ID for idempotency deduplication", example = "GW-MSG-100293")
            String externalRecordId,

            @NotNull(message = "Field ID is required")
            @Schema(description = "UUID of target parcel")
            UUID fieldId,

            @Schema(description = "UUID of deployed sensor device (if known)")
            UUID sensorId,

            @Schema(description = "Edge hardware device identifier string (e.g. EUI-70B3D5...)")
            String deviceIdentifier,

            @NotNull(message = "Sensor type is required")
            @Schema(description = "Observed parameter type", example = "SOIL_MOISTURE")
            SensorType sensorType,

            @NotNull(message = "Value is required")
            @Schema(description = "Observed numeric reading", example = "42.75")
            BigDecimal value,

            @Schema(description = "Raw measurement unit", example = "%")
            String unit,

            @Schema(description = "Observation instant in UTC")
            Instant measuredAt,

            @Schema(description = "Evidence classification (defaults to OBSERVED)")
            String evidenceClassification
    ) {
        public IngestTelemetryItemCommand toCommand() {
            return new IngestTelemetryItemCommand(
                    externalRecordId,
                    fieldId,
                    sensorId,
                    deviceIdentifier,
                    sensorType,
                    value,
                    unit,
                    measuredAt,
                    evidenceClassification
            );
        }
    }

    public IngestBatchCommand toCommand() {
        return new IngestBatchCommand(
                sourceCode,
                items.stream().map(ItemRequest::toCommand).toList()
        );
    }
}
