package com.aquaai.field.api.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request body for registering a new agricultural field parcel")
public record CreateFieldRequest(
        @NotBlank(message = "Field name cannot be blank")
        @Size(max = 128, message = "Field name cannot exceed 128 characters")
        @Schema(description = "User-assigned name of the field parcel", example = "Paxtakor Plot 1")
        String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        @Schema(description = "Optional narrative notes or landmarks", example = "Cotton field adjacent to canal branch 3")
        String description,

        @NotNull(message = "Field area in hectares is mandatory")
        @Positive(message = "Field area must be greater than zero hectares")
        @Schema(description = "Field parcel surface area in canonical Hectares (ha)", example = "12.5000")
        BigDecimal areaHectares,

        @NotNull(message = "Crop type is mandatory")
        @Schema(description = "Agricultural crop currently cultivated", example = "COTTON")
        CropType cropType,

        @NotNull(message = "Soil type is mandatory")
        @Schema(description = "Soil hydrological classification", example = "LOAM")
        SoilType soilType,

        @NotNull(message = "Irrigation method is mandatory")
        @Schema(description = "Water application technique", example = "FURROW")
        IrrigationMethod irrigationMethod,

        @DecimalMin(value = "-90.0", message = "Latitude must be between -90.0 and 90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90.0 and 90.0")
        @Schema(description = "Field centroid latitude in degrees WGS84", example = "41.450123")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180.0 and 180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180.0 and 180.0")
        @Schema(description = "Field centroid longitude in degrees WGS84", example = "60.590456")
        BigDecimal longitude,

        @Valid
        @Schema(description = "GeoJSON RFC 7946 Polygon boundary")
        FieldBoundary boundary
) {
}
