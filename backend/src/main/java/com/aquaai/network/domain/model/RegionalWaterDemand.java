package com.aquaai.network.domain.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Aggregated regional water demand summary at a given hierarchical tier
 * (DISTRICT, WUA, PRIMARY_CANAL, SECONDARY_CANAL, DISTRIBUTION_POINT, FIELD).
 */
@Builder
public record RegionalWaterDemand(
        String targetLevel,
        UUID entityId,
        String entityName,
        BigDecimal totalRequestedVolumeM3,
        BigDecimal totalAllocatedVolumeM3,
        BigDecimal totalDeliveredVolumeM3,
        BigDecimal totalUpcomingVolumeM3,
        int activeParcelCount,
        BigDecimal peakFlowRateM3S,
        Instant windowStart,
        Instant windowEnd
) {
}
