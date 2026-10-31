package com.aquaai.field.api.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Compact agricultural field parcel summary representation.
 * Intended for lightweight listings and dropdown selectors where boundary geometry is not needed.
 */
@Schema(description = "Compact agricultural field parcel summary representation")
public record FieldSummaryResponse(
        @Schema(description = "Unique field parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID id,

        @Schema(description = "Name of the agricultural field", example = "Paxtakor Plot 1")
        String name,

        @Schema(description = "Description or location notes", example = "Main cotton field near Yangiariq canal")
        String description,

        @Schema(description = "Field area in canonical Hectares (ha)", example = "12.5000")
        BigDecimal areaHectares,

        @Schema(description = "Crop currently cultivated", example = "COTTON")
        CropType cropType,

        @Schema(description = "Soil hydrological classification", example = "LOAM")
        SoilType soilType,

        @Schema(description = "Irrigation delivery system", example = "FURROW")
        IrrigationMethod irrigationMethod,

        @Schema(description = "Parcel lifecycle status", example = "ACTIVE")
        FieldStatus status,

        @Schema(description = "Parcel registration timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static FieldSummaryResponse from(Field field) {
        if (field == null) {
            return null;
        }
        return new FieldSummaryResponse(
                field.getId(),
                field.getName(),
                field.getDescription(),
                field.getAreaHectares(),
                field.getCropType(),
                field.getSoilType(),
                field.getIrrigationMethod(),
                field.getStatus(),
                field.getCreatedAt(),
                field.getUpdatedAt()
        );
    }
}
