package com.aquaai.water.domain.repository;

import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for {@link IrrigationEvent} persistence.
 */
public interface IrrigationEventRepository {

    IrrigationEvent save(IrrigationEvent event);

    Optional<IrrigationEvent> findById(UUID id);

    Optional<IrrigationEvent> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    List<IrrigationEvent> findByFieldId(UUID fieldId);

    List<IrrigationEvent> findByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status);

    List<IrrigationEvent> findByIrrigationPlanId(UUID irrigationPlanId);

    List<IrrigationEvent> findByFieldIdAndStartedAtBetween(UUID fieldId, Instant start, Instant end);

    boolean existsById(UUID id);

    long countByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status);

    BigDecimal sumActualVolumeByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status);

    com.aquaai.water.domain.model.IrrigationEventPage findWithFilters(
            java.util.Collection<UUID> fieldIds,
            UUID irrigationPlanId,
            IrrigationEventStatus status,
            Instant startDate,
            Instant endDate,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    void deleteById(UUID id);
}
