package com.aquaai.gis.api.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Spatial and agronomic summary of an agricultural parcel for GIS map rendering")
public record GisFieldDto(
        @Schema(description = "Unique field parcel identifier")
        UUID id,

        @Schema(description = "Owner user identifier")
        UUID ownerId,

        @Schema(description = "Parcel name", example = "Paxta dalasi 12")
        String name,

        @Schema(description = "Parcel description", example = "North section near secondary canal")
        String description,

        @Schema(description = "Authoritative registered surface area in hectares", example = "8.4000")
        BigDecimal areaHectares,

        @Schema(description = "Cultivated crop type", example = "COTTON")
        CropType cropType,

        @Schema(description = "Agronomic soil classification", example = "LOAM")
        SoilType soilType,

        @Schema(description = "Registered irrigation technique", example = "DRIP")
        IrrigationMethod irrigationMethod,

        @Schema(description = "Representative centroid latitude coordinate", example = "41.341400")
        BigDecimal latitude,

        @Schema(description = "Representative centroid longitude coordinate", example = "60.609400")
        BigDecimal longitude,

        @Schema(description = "GeoJSON RFC 7946 Polygon boundary contour")
        FieldBoundary boundary,

        @Schema(description = "Operational status of parcel", example = "ACTIVE")
        FieldStatus status
) {
    public static GisFieldDto from(Field field) {
        if (field == null) {
            return null;
        }
        return new GisFieldDto(
                field.getId(),
                field.getOwnerId(),
                field.getName(),
                field.getDescription(),
                field.getAreaHectares(),
                field.getCropType(),
                field.getSoilType(),
                field.getIrrigationMethod(),
                field.getLocation() != null ? field.getLocation().latitude() : null,
                field.getLocation() != null ? field.getLocation().longitude() : null,
                field.getBoundary(),
                field.getStatus()
        );
    }
}
