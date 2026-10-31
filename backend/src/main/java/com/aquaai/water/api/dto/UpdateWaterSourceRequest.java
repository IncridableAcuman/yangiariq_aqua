package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.WaterSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating water source infrastructure details")
public record UpdateWaterSourceRequest(
        @NotBlank(message = "Water source name cannot be blank")
        @Size(max = 128, message = "Water source name cannot exceed 128 characters")
        @Schema(description = "Updated infrastructure name", example = "Palvon-Yobi Upgraded Canal")
        String name,

        @NotNull(message = "Water source type is mandatory")
        @Schema(description = "Hydraulic classification", example = "CANAL")
        WaterSourceType type,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Operational notes", example = "Concrete lined section completed")
        String description
) {
}
