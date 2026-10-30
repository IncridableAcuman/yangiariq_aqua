package com.aquaai.field.application.dto;

import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldBoundary;
import com.aquaai.field.domain.model.FieldLocation;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for updating an existing agricultural field parcel.
 */
public record UpdateFieldCommand(
        UUID fieldId,
        UUID currentUserId,
        String name,
        String description,
        BigDecimal areaHectares,
        CropType cropType,
        SoilType soilType,
        IrrigationMethod irrigationMethod,
        FieldLocation location,
        FieldBoundary boundary
) {
}
