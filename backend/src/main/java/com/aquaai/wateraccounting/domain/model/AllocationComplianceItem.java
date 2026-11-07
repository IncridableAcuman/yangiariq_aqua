package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Compliance evaluation record comparing authorized water allocations against physical deliveries and consumptions.
 */
@Getter
@Builder(toBuilder = true)
public class AllocationComplianceItem {

    private final UUID targetId;
    private final String targetName;
    private final String targetType; // FIELD, FARMER, WUA
    private final String wuaName;
    private final String cropType;
    private final BigDecimal areaHectares;
    private final BigDecimal allocatedVolumeM3;
    private final BigDecimal reservedVolumeM3;
    private final BigDecimal approvedDispatchVolumeM3;
    private final BigDecimal actualDeliveredVolumeM3;
    private final BigDecimal actualConsumptionM3;
    private final BigDecimal remainingAllocationM3;
    private final BigDecimal overDeliveryM3;
    private final BigDecimal underDeliveryM3;
    private final BigDecimal deliveryCompletionPercent;
    private final AllocationComplianceStatus status;
    private final EvidenceClassification evidenceClassification;
    private final String notes;

    public AllocationComplianceItem(
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
            AllocationComplianceStatus status,
            EvidenceClassification evidenceClassification,
            String notes
    ) {
        this.targetId = targetId;
        this.targetName = targetName != null ? targetName : "Field";
        this.targetType = targetType != null ? targetType : "FIELD";
        this.wuaName = wuaName != null ? wuaName : "WUA";
        this.cropType = cropType != null ? cropType : "COTTON";
        this.areaHectares = areaHectares != null ? areaHectares : BigDecimal.ONE;
        this.allocatedVolumeM3 = allocatedVolumeM3 != null ? allocatedVolumeM3 : BigDecimal.ZERO;
        this.reservedVolumeM3 = reservedVolumeM3 != null ? reservedVolumeM3 : BigDecimal.ZERO;
        this.approvedDispatchVolumeM3 = approvedDispatchVolumeM3 != null ? approvedDispatchVolumeM3 : BigDecimal.ZERO;
        this.actualDeliveredVolumeM3 = actualDeliveredVolumeM3 != null ? actualDeliveredVolumeM3 : BigDecimal.ZERO;
        this.actualConsumptionM3 = actualConsumptionM3 != null ? actualConsumptionM3 : BigDecimal.ZERO;
        this.remainingAllocationM3 = remainingAllocationM3 != null ? remainingAllocationM3 : BigDecimal.ZERO;
        this.overDeliveryM3 = overDeliveryM3 != null ? overDeliveryM3 : BigDecimal.ZERO;
        this.underDeliveryM3 = underDeliveryM3 != null ? underDeliveryM3 : BigDecimal.ZERO;
        this.deliveryCompletionPercent = deliveryCompletionPercent != null ? deliveryCompletionPercent : BigDecimal.ZERO;
        this.status = status != null ? status : AllocationComplianceStatus.ALLOCATION_RESPECTED;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.notes = notes;
    }
}
