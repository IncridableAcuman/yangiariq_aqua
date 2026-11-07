package com.aquaai.wateraccounting.domain.service;

import com.aquaai.wateraccounting.domain.model.AllocationComplianceItem;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceStatus;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Pure domain service evaluating water delivery and consumption compliance against authorized quota allocations.
 */
public class AllocationComplianceEngine {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public AllocationComplianceItem evaluateCompliance(
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
            EvidenceClassification classification
    ) {
        BigDecimal area = areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) > 0 ? areaHectares : BigDecimal.ONE;
        BigDecimal allocated = allocatedVolumeM3 != null ? allocatedVolumeM3 : BigDecimal.ZERO;
        BigDecimal reserved = reservedVolumeM3 != null ? reservedVolumeM3 : BigDecimal.ZERO;
        BigDecimal approvedDispatch = approvedDispatchVolumeM3 != null ? approvedDispatchVolumeM3 : BigDecimal.ZERO;
        BigDecimal delivered = actualDeliveredVolumeM3 != null ? actualDeliveredVolumeM3 : BigDecimal.ZERO;
        BigDecimal consumed = actualConsumptionM3 != null ? actualConsumptionM3 : BigDecimal.ZERO;

        if (allocated.compareTo(BigDecimal.ZERO) <= 0) {
            return AllocationComplianceItem.builder()
                    .targetId(targetId)
                    .targetName(targetName)
                    .targetType(targetType)
                    .wuaName(wuaName)
                    .cropType(cropType)
                    .areaHectares(area)
                    .allocatedVolumeM3(BigDecimal.ZERO)
                    .reservedVolumeM3(reserved)
                    .approvedDispatchVolumeM3(approvedDispatch)
                    .actualDeliveredVolumeM3(delivered)
                    .actualConsumptionM3(consumed)
                    .remainingAllocationM3(BigDecimal.ZERO)
                    .overDeliveryM3(BigDecimal.ZERO)
                    .underDeliveryM3(BigDecimal.ZERO)
                    .deliveryCompletionPercent(BigDecimal.ZERO)
                    .status(AllocationComplianceStatus.INSUFFICIENT_DATA)
                    .evidenceClassification(EvidenceClassification.MISSING)
                    .notes("No active seasonal water allocation found for this entity.")
                    .build();
        }

        BigDecimal remaining = allocated.subtract(consumed);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        BigDecimal overDelivery = delivered.subtract(allocated);
        if (overDelivery.compareTo(BigDecimal.ZERO) < 0) overDelivery = BigDecimal.ZERO;

        BigDecimal underDelivery = approvedDispatch.subtract(delivered);
        if (underDelivery.compareTo(BigDecimal.ZERO) < 0) underDelivery = BigDecimal.ZERO;

        BigDecimal completionPercent = delivered.multiply(new BigDecimal("100.00")).divide(allocated, 2, ROUNDING);

        AllocationComplianceStatus status;
        String notes;

        if (consumed.compareTo(allocated) > 0) {
            status = AllocationComplianceStatus.ALLOCATION_EXCEEDED;
            notes = String.format("Consumption exceeds authorized allocation quota by %.2f m³.", consumed.subtract(allocated));
        } else if (consumed.compareTo(allocated.multiply(new BigDecimal("0.90"))) >= 0) {
            status = AllocationComplianceStatus.APPROACHING_LIMIT;
            notes = String.format("Quota nearly exhausted: %.2f%% consumed (%.2f m³ remaining).", completionPercent, remaining);
        } else if (approvedDispatch.compareTo(BigDecimal.ZERO) > 0 && delivered.compareTo(approvedDispatch.multiply(new BigDecimal("0.80"))) < 0) {
            status = AllocationComplianceStatus.UNDER_DELIVERED;
            notes = String.format("Delivery shortfall: Received %.2f m³ vs approved dispatch of %.2f m³.", delivered, approvedDispatch);
        } else if (delivered.compareTo(BigDecimal.ZERO) == 0 && approvedDispatch.compareTo(BigDecimal.ZERO) > 0) {
            status = AllocationComplianceStatus.UNVERIFIED_DELIVERY;
            notes = "Approved dispatch exists but zero delivery has been confirmed by operator.";
        } else {
            status = AllocationComplianceStatus.ALLOCATION_RESPECTED;
            notes = String.format("Water usage compliant (%.2f%% of quota delivered, %.2f m³ remaining).", completionPercent, remaining);
        }

        return AllocationComplianceItem.builder()
                .targetId(targetId)
                .targetName(targetName)
                .targetType(targetType)
                .wuaName(wuaName)
                .cropType(cropType)
                .areaHectares(area)
                .allocatedVolumeM3(allocated.setScale(2, ROUNDING))
                .reservedVolumeM3(reserved.setScale(2, ROUNDING))
                .approvedDispatchVolumeM3(approvedDispatch.setScale(2, ROUNDING))
                .actualDeliveredVolumeM3(delivered.setScale(2, ROUNDING))
                .actualConsumptionM3(consumed.setScale(2, ROUNDING))
                .remainingAllocationM3(remaining.setScale(2, ROUNDING))
                .overDeliveryM3(overDelivery.setScale(2, ROUNDING))
                .underDeliveryM3(underDelivery.setScale(2, ROUNDING))
                .deliveryCompletionPercent(completionPercent)
                .status(status)
                .evidenceClassification(classification != null ? classification : EvidenceClassification.CALCULATED)
                .notes(notes)
                .build();
    }
}
