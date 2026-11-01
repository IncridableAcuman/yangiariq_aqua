package com.aquaai.irrigation.domain.port;

import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for persisting and querying {@link SmartIrrigationPlan} aggregate roots.
 */
public interface SmartIrrigationPlanRepository {

    SmartIrrigationPlan save(SmartIrrigationPlan plan);

    Optional<SmartIrrigationPlan> findById(UUID id);

    List<SmartIrrigationPlan> findByFieldId(UUID fieldId);

    Optional<SmartIrrigationPlan> findActiveByFieldId(UUID fieldId);

    List<SmartIrrigationPlan> findByPlannedDate(LocalDate plannedDate);

    boolean existsActivePlanForDate(UUID fieldId, LocalDate plannedDate);

    BigDecimal sumVolumeByFieldIdAndStatuses(UUID fieldId, Collection<IrrigationPlanStatus> statuses);

    List<SmartIrrigationPlan> findFiltered(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int size
    );

    long countFiltered(
            UUID fieldId,
            IrrigationPlanStatus status,
            IrrigationPriority priority,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    void deleteById(UUID id);
}
