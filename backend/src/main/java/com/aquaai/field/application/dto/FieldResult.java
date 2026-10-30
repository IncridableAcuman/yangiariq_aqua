package com.aquaai.field.application.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable application result representing field data.
 * Protects domain aggregates and JPA entities from being directly leaked across application boundaries.
 */
public record FieldResult(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        BigDecimal areaHectares,
        CropType cropType,
        SoilType soilType,
        IrrigationMethod irrigationMethod,
        FieldLocation location,
        FieldBoundary boundary,
        FieldStatus status,
        Instant createdAt,
        Instant updatedAt,
        Long version
) {

    public static FieldResult from(Field field) {
        if (field == null) {
            return null;
        }
        return new FieldResult(
                field.getId(),
                field.getOwnerId(),
                field.getName(),
                field.getDescription(),
                field.getAreaHectares(),
                field.getCropType(),
                field.getSoilType(),
                field.getIrrigationMethod(),
                field.getLocation(),
                field.getBoundary(),
                field.getStatus(),
                field.getCreatedAt(),
                field.getUpdatedAt(),
                field.getVersion()
        );
    }
}
