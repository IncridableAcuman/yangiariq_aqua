package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AllocationComplianceResponse(
        UUID targetId,
        String targetName,
        String targetType,
        String wuaName,
        String cropType,
        BigDecimal areaHectares,
        BigDecimal allocatedVolumeM3,
        BigDecimal reservedVolumeM3,
        BigDecimal approvedDispatchVolumeM3,
        BigDecimal actualDeliveredVolumeM3,
        BigDecimal actualConsumptionM3,
        BigDecimal remainingAllocationM3,
        BigDecimal overDeliveryM3,
        BigDecimal underDeliveryM3,
        BigDecimal deliveryCompletionPercent,
        String status,
        String evidenceClassification,
        String notes
) {}
