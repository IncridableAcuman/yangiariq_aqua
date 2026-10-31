package com.aquaai.water.domain.repository;

import com.aquaai.water.domain.model.IrrigationPlan;
import com.aquaai.water.domain.model.IrrigationPlanStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for {@link IrrigationPlan} persistence.
 */
public interface IrrigationPlanRepository {

    IrrigationPlan save(IrrigationPlan plan);

    Optional<IrrigationPlan> findById(UUID id);

    Optional<IrrigationPlan> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    List<IrrigationPlan> findByFieldId(UUID fieldId);

    List<IrrigationPlan> findByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status);

    List<IrrigationPlan> findByWaterAllocationId(UUID waterAllocationId);

    List<IrrigationPlan> findByFieldIdAndPlannedStartAtBetween(UUID fieldId, Instant start, Instant end);

    boolean existsById(UUID id);

    long countByFieldIdAndStatus(UUID fieldId, IrrigationPlanStatus status);

    com.aquaai.water.domain.model.IrrigationPlanPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID waterAllocationId,
            IrrigationPlanStatus status,
            Instant startDate,
            Instant endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    void deleteById(UUID id);
}
