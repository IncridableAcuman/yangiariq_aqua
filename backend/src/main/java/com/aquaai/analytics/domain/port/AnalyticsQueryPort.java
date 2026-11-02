package com.aquaai.analytics.domain.port;

import com.aquaai.analytics.domain.model.TrendGranularity;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationEventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain port for batch analytical queries, historical metrics, and performance trend projections.
 * Enforces zero N+1 queries by aggregating across collections of parcel IDs.
 */
public interface AnalyticsQueryPort {

    /**
     * Retrieves aggregated water allocation quotas and consumed volumes per field.
     */
    Map<UUID, FieldWaterSummaryData> getFieldWaterSummaries(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Groups Smart Irrigation Plans by field ID and status, returning counts and total recommended volumes.
     */
    Map<UUID, Map<IrrigationPlanStatus, Long>> getPlanStatusCounts(Collection<UUID> fieldIds, LocalDate fromDate, LocalDate toDate);

    Map<UUID, BigDecimal> getPlannedVolumes(Collection<UUID> fieldIds, LocalDate fromDate, LocalDate toDate);

    /**
     * Groups Irrigation Events by field ID and status, returning counts and actual delivered volumes.
     */
    Map<UUID, Map<IrrigationEventStatus, Long>> getEventStatusCounts(Collection<UUID> fieldIds, Instant from, Instant to);

    Map<UUID, BigDecimal> getDeliveredVolumes(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Calculates average planned duration and actual execution duration in minutes for completed operations.
     */
    Map<UUID, Integer> getAverageActualDurationsMinutes(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Groups anomalies by field ID and severity within the temporal window.
     */
    Map<UUID, Map<AnomalySeverity, Long>> getAnomalyCounts(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Groups sensor devices by field ID and operational status.
     */
    Map<UUID, Map<SensorStatus, Integer>> getSensorStatusCounts(Collection<UUID> fieldIds);

    /**
     * Groups telemetry readings quality by field ID within the temporal window.
     */
    Map<UUID, Map<ReadingQuality, Long>> getReadingQualityCounts(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Returns the count of verified completed operations per field.
     */
    Map<UUID, Long> getVerifiedOperationsCount(Collection<UUID> fieldIds, Instant from, Instant to);

    /**
     * Aggregates chronological trend data grouped by the specified granularity.
     */
    List<TrendBucketRawData> getChronologicalTrendBuckets(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to,
            TrendGranularity granularity
    );

    record TrendBucketRawData(
            Instant bucketTimestamp,
            String dateLabel,
            BigDecimal waterConsumedM3,
            BigDecimal waterPlannedM3,
            int completedOperationsCount,
            long totalReadingsCount,
            long validReadingsCount,
            int safetyEventsCount
    ) {}
}
