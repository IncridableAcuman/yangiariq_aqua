package com.aquaai.water.domain.repository;

import com.aquaai.water.domain.model.WaterAllocation;
import com.aquaai.water.domain.model.WaterAllocationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for {@link WaterAllocation} persistence.
 */
public interface WaterAllocationRepository {

    WaterAllocation save(WaterAllocation allocation);

    Optional<WaterAllocation> findById(UUID id);

    Optional<WaterAllocation> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    List<WaterAllocation> findByFieldId(UUID fieldId);

    List<WaterAllocation> findByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status);

    List<WaterAllocation> findByWaterSourceId(UUID waterSourceId);

    List<WaterAllocation> findByWaterSourceIdAndStatus(UUID waterSourceId, WaterAllocationStatus status);

    boolean existsById(UUID id);

    long countByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status);

    BigDecimal sumAllocatedVolumeByFieldIdAndStatus(UUID fieldId, WaterAllocationStatus status);

    com.aquaai.water.domain.model.WaterAllocationPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID waterSourceId,
            WaterAllocationStatus status,
            java.time.Instant periodStart,
            java.time.Instant periodEnd,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    void deleteById(UUID id);
}
