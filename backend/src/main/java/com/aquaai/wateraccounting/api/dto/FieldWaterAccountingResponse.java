package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record FieldWaterAccountingResponse(
        UUID fieldId,
        String fieldName,
        UUID ownerId,
        String wuaName,
        BigDecimal areaHectares,
        String cropType,
        AllocationComplianceResponse compliance,
        List<WaterLedgerEntryResponse> recentMovements,
        List<ConservationOpportunityResponse> recommendations
) {}
