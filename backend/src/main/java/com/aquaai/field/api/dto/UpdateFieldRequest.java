package com.aquaai.field.api.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body for modifying an existing field parcel. Omitted fields remain unchanged.")
public record UpdateFieldRequest(
        @Size(max = 128, message = "Field name cannot exceed 128 characters")
        @Schema(description = "Updated name of the field parcel", example = "Paxtakor Plot 1 Updated")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Updated narrative notes", example = "Deep tilled in autumn")
        String description,

        @Positive(message = "Field area must be greater than zero hectares")
        @Schema(description = "Updated field surface area in Hectares (ha)", example = "15.0000")
        BigDecimal areaHectares,

        @Schema(description = "Updated agricultural crop classification", example = "WHEAT")
        CropType cropType,

        @Schema(description = "Updated soil classification", example = "SANDY_LOAM")
        SoilType soilType,

        @Schema(description = "Updated irrigation method", example = "DRIP")
        IrrigationMethod irrigationMethod,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90.0 and 90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90.0 and 90.0")
        @Schema(description = "Updated centroid latitude", example = "41.451000")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180.0 and 180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180.0 and 180.0")
        @Schema(description = "Updated centroid longitude", example = "60.591000")
        BigDecimal longitude,

        @Valid
        @Schema(description = "Updated GeoJSON RFC 7946 Polygon boundary")
        FieldBoundary boundary
) {
}
