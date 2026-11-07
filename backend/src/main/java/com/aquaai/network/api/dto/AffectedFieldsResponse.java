package com.aquaai.network.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AffectedFieldsResponse(
        UUID edgeId,
        String edgeName,
        int dependentFieldCount,
        List<AffectedFieldItem> dependentFields
) {
    public record AffectedFieldItem(
            UUID fieldId,
            String name,
            String cropType,
            BigDecimal areaHectares,
            String ownerName
    ) {}
}
