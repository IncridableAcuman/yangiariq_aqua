package com.aquaai.integration.api.dto;

import com.aquaai.integration.application.dto.RecordManualMeasurementCommand;
import com.aquaai.sensor.domain.model.SensorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordManualMeasurementRequest(
        @NotNull(message = "Field ID is required")
        @Schema(description = "UUID of the field parcel")
        UUID fieldId,

        @Schema(description = "Optional UUID of specific sensor device")
        UUID sensorId,

        @NotNull(message = "Sensor type is required")
        @Schema(description = "Agronomic parameter type (e.g. SOIL_MOISTURE, WATER_FLOW)", example = "SOIL_MOISTURE")
        SensorType sensorType,

        @NotNull(message = "Measurement value is required")
        @Schema(description = "Observed numeric value", example = "48.5")
        BigDecimal value,

        @Schema(description = "Unit string (e.g. %, m³/h, L/s, bar, °C)", example = "%")
        String unit,

        @Schema(description = "Observation timestamp in UTC (defaults to current time)")
        Instant measuredAt,

        @Schema(description = "Whether the measurement has been physically verified by field operator")
        boolean verified,

        @Schema(description = "Operator notes or calibration context")
        String notes
) {
    public RecordManualMeasurementCommand toCommand() {
        return new RecordManualMeasurementCommand(
                fieldId,
                sensorId,
                sensorType,
                value,
                unit,
                measuredAt,
                verified,
                notes
        );
    }
}
