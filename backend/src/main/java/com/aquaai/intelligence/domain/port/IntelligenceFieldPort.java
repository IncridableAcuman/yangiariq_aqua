package com.aquaai.intelligence.domain.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for retrieving field parcel agronomic parameters.
 */
public interface IntelligenceFieldPort {

    record ParcelDetails(
            UUID fieldId,
            UUID ownerId,
            String cropType,
            String soilType,
            BigDecimal areaHectares
    ) {}

    Optional<ParcelDetails> getFieldDetails(UUID fieldId);
}
