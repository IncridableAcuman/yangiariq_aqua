package com.aquaai.water.domain.repository;

import com.aquaai.water.domain.model.FieldWaterSummaryData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Domain query repository port for analytical water consumption aggregates,
 * quota tracking, planned vs actual variance lookups, and field summaries.
 */
public interface WaterConsumptionQueryRepository {

    /**
     * Calculates the sum of actual consumed water (m³) across all COMPLETED events
     * associated with the given allocation.
     *
     * @param allocationId Quota allocation identifier
     * @return Total volume consumed (non-null, >= 0)
     */
    BigDecimal getConsumedVolumeByAllocationId(UUID allocationId);

    /**
     * Batch-calculates consumed water volumes for a collection of allocation IDs.
     * Prevents N+1 database queries during allocation listings.
     *
     * @param allocationIds Collection of allocation identifiers
     * @return Map of allocationId to consumedVolumeM3
     */
    Map<UUID, BigDecimal> getConsumedVolumesByAllocationIds(Collection<UUID> allocationIds);

    /**
     * Counts COMPLETED irrigation events associated with an allocation.
     *
     * @param allocationId Quota allocation identifier
     * @return Count of completed events
     */
    long countCompletedEventsByAllocationId(UUID allocationId);

    /**
     * Calculates the sum of actual delivered water (m³) across all COMPLETED events
     * associated with the scheduled irrigation plan.
     *
     * @param planId Irrigation plan identifier
     * @return Total actual volume delivered (non-null, >= 0)
     */
    BigDecimal getConsumedVolumeByPlanId(UUID planId);

    /**
     * Batch-calculates actual delivered water volumes for a collection of plan IDs.
     * Prevents N+1 database queries during plan listings.
     *
     * @param planIds Collection of plan identifiers
     * @return Map of planId to actualVolumeM3
     */
    Map<UUID, BigDecimal> getConsumedVolumesByPlanIds(Collection<UUID> planIds);

    /**
     * Counts COMPLETED irrigation events associated with an irrigation plan.
     *
     * @param planId Irrigation plan identifier
     * @return Count of completed events
     */
    long countCompletedEventsByPlanId(UUID planId);

    /**
     * Aggregates parcel-level total allocated quotas and total executed consumption
     * within the specified optional temporal window.
     *
     * @param fieldId Field parcel identifier
     * @param from    Filter window start timestamp (inclusive, nullable)
     * @param to      Filter window end timestamp (inclusive, nullable)
     * @return Aggregated {@link FieldWaterSummaryData}
     */
    FieldWaterSummaryData getFieldWaterSummary(UUID fieldId, Instant from, Instant to);

    /**
     * Batch-aggregates parcel-level total allocated quotas and total executed consumption
     * for a collection of field IDs within the specified optional temporal window.
     * Completely prevents N+1 queries during district/GIS map overviews.
     *
     * @param fieldIds Collection of field parcel identifiers
     * @param from     Filter window start timestamp (inclusive, nullable)
     * @param to       Filter window end timestamp (inclusive, nullable)
     * @return Map of fieldId to aggregated {@link FieldWaterSummaryData}
     */
    Map<UUID, FieldWaterSummaryData> getFieldWaterSummaries(Collection<UUID> fieldIds, Instant from, Instant to);
}
