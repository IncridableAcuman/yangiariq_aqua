package com.aquaai.field.api.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Agricultural field parcel response representation")
public record FieldResponse(
        @Schema(description = "Unique immutable identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
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

        @Schema(description = "Centroid latitude WGS84", example = "41.450123")
        BigDecimal latitude,

        @Schema(description = "Centroid longitude WGS84", example = "60.590456")
        BigDecimal longitude,

        @Schema(description = "GeoJSON RFC 7946 Polygon boundary")
        FieldBoundary boundary,

        @Schema(description = "Parcel lifecycle status", example = "ACTIVE")
        FieldStatus status,

        @Schema(description = "Parcel registration timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static FieldResponse from(Field field) {
        if (field == null) {
            return null;
        }
        BigDecimal lat = field.getLocation() != null ? field.getLocation().latitude() : null;
        BigDecimal lon = field.getLocation() != null ? field.getLocation().longitude() : null;

        return new FieldResponse(
                field.getId(),
                field.getName(),
                field.getDescription(),
                field.getAreaHectares(),
                field.getCropType(),
                field.getSoilType(),
                field.getIrrigationMethod(),
                lat,
                lon,
                field.getBoundary(),
                field.getStatus(),
                field.getCreatedAt(),
                field.getUpdatedAt()
        );
    }
}
