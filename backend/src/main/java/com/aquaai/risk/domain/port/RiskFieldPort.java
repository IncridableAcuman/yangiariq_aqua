package com.aquaai.risk.domain.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskFieldPort {

    record FieldInfo(
            UUID id,
            UUID ownerId,
            String name,
            BigDecimal areaHectares,
            String cropType,
            String soilType,
            String irrigationMethod
    ) {}

    List<UUID> getAccessibleFieldIds(UUID currentUserId, boolean isDistrictScope);

    Optional<FieldInfo> getFieldInfo(UUID fieldId);

    List<FieldInfo> getAllAccessibleFieldInfos(UUID currentUserId, boolean isDistrictScope);
}
