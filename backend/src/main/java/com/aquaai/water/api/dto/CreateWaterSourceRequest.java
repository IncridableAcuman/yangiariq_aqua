package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.WaterSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body for registering a regional water source infrastructure entity")
public record CreateWaterSourceRequest(
        @NotBlank(message = "Water source name cannot be blank")
        @Size(max = 128, message = "Water source name cannot exceed 128 characters")
        @Schema(description = "Hydraulic infrastructure or canal name", example = "Palvon-Yobi Main Canal")
        String name,

        @NotNull(message = "Water source type is mandatory")
        @Schema(description = "Hydraulic classification of source", example = "CANAL")
        WaterSourceType type,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Operational notes, intake branch, or regional scope", example = "Main feeder canal serving western Yangiariq parcels")
        String description,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90.0 and 90.0 degrees")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90.0 and 90.0 degrees")
        @Schema(description = "Representative intake or infrastructure latitude", example = "41.345678")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180.0 and 180.0 degrees")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180.0 and 180.0 degrees")
        @Schema(description = "Representative intake or infrastructure longitude", example = "60.612345")
        BigDecimal longitude
) {
    public CreateWaterSourceRequest(String name, WaterSourceType type, String description) {
        this(name, type, description, null, null);
    }
}
