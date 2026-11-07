package com.aquaai.wateraccounting.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountingFieldPort {

    List<FieldSummary> getAllFields();

    Optional<FieldSummary> getFieldById(UUID fieldId);

    List<FieldSummary> getFieldsByOwner(UUID ownerId);

    record FieldSummary(
            UUID id,
            String name,
            UUID ownerId,
            BigDecimal areaHectares,
            String cropType,
            String soilType
    ) {}
}
