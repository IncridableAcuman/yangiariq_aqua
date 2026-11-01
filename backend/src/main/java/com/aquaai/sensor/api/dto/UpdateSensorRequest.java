package com.aquaai.sensor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating sensor details")
public record UpdateSensorRequest(
        @Size(max = 128, message = "Sensor name cannot exceed 128 characters")
        @Schema(description = "Updated label for sensor", example = "Yangilangan Datchik Nomi")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Updated description", example = "Datchik yangilandi va qayta sozlandi")
        String description
) {
}
