package com.aquaai.gis.api.dto;

import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Regional water infrastructure distribution point for GIS mapping")
public record GisWaterSourceDto(
        @Schema(description = "Unique water source identifier")
        UUID id,

        @Schema(description = "Infrastructure name", example = "Palvon-Yobi Main Canal")
        String name,

        @Schema(description = "Hydraulic classification", example = "CANAL")
        WaterSourceType type,

        @Schema(description = "Operational description", example = "Primary delivery canal")
        String description,

        @Schema(description = "Infrastructure operational status", example = "ACTIVE")
        WaterSourceStatus status,

        @Schema(description = "Representative geographic latitude", example = "41.345678")
        BigDecimal latitude,

        @Schema(description = "Representative geographic longitude", example = "60.612345")
        BigDecimal longitude
) {
    public static GisWaterSourceDto from(WaterSource source) {
        if (source == null) {
            return null;
        }
        return new GisWaterSourceDto(
                source.getId(),
                source.getName(),
                source.getType(),
                source.getDescription(),
                source.getStatus(),
                source.getLatitude(),
                source.getLongitude()
        );
    }
}
