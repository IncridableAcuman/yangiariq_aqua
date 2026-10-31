package com.aquaai.water.domain.model;

import java.math.BigDecimal;

/**
 * Raw aggregated query payload returned by the persistence adapter for parcel field queries.
 *
 * @param totalAllocatedVolumeM3 Sum of allocated quotas in the period
 * @param allocationCount        Number of active/closed quota records
 * @param totalConsumedVolumeM3  Sum of actual water delivered across completed events
 * @param irrigationEventCount   Number of completed irrigation operations
 */
public record FieldWaterSummaryData(
        BigDecimal totalAllocatedVolumeM3,
        long allocationCount,
        BigDecimal totalConsumedVolumeM3,
        long irrigationEventCount
) {
}
