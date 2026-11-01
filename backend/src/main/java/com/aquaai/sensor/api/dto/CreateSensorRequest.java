package com.aquaai.sensor.api.dto;

import com.aquaai.sensor.domain.model.SensorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request payload for registering and deploying a new sensor device on a field")
public record CreateSensorRequest(
        @NotNull(message = "Field ID is required")
        @Schema(description = "Target field parcel UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
        UUID fieldId,

        @NotBlank(message = "Sensor name is required")
        @Size(max = 128, message = "Sensor name cannot exceed 128 characters")
        @Schema(description = "Human-readable label for sensor", example = "Tuproq Namlik Datchigi #1")
        String name,

        @NotBlank(message = "Device identifier is required")
        @Size(max = 64, message = "Device identifier cannot exceed 64 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_\\-\\.:]+$", message = "Device identifier must consist of alphanumeric characters, dashes, dots, or underscores")
        @Schema(description = "Unique physical/hardware device serial or tag", example = "SN-SOIL-001")
        String deviceIdentifier,

        @NotNull(message = "Sensor type is required")
        @Schema(description = "Agronomic telemetry sensor type", example = "SOIL_MOISTURE")
        SensorType type,

        @Size(max = 64, message = "Provider identifier cannot exceed 64 characters")
        @Schema(description = "Telemetry ingestion provider (e.g. SIMULATED, MQTT, HTTP)", example = "SIMULATED")
        String provider,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Operational notes or placement details", example = "Placed in root zone at 30cm depth")
        String description
) {
}
