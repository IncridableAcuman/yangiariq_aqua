package com.aquaai.irrigation.domain.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for retrieving field parcel agronomic characteristics.
 */
public interface FieldDataPort {

    record FieldAgronomicData(
            UUID fieldId,
            String name,
            UUID ownerId,
            BigDecimal areaHectares,
            String cropType,
            String soilType,
            String irrigationMethod
    ) {}

    Optional<FieldAgronomicData> getFieldData(UUID fieldId);
}
