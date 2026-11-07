package com.aquaai.network.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NetworkFieldPort {

    record FieldParcelInfo(
            UUID id,
            String name,
            UUID ownerId,
            BigDecimal areaHectares,
            String cropType,
            String soilType,
            String irrigationMethod,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}

    Optional<FieldParcelInfo> getFieldInfo(UUID fieldId);
    List<FieldParcelInfo> getAllFields();
    List<UUID> getFieldIdsByOwner(UUID ownerId);
}
