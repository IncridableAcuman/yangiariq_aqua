package com.aquaai.wateraccounting.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AccountingWaterPort {

    List<AllocationSummary> getAllAllocations();

    List<AllocationSummary> getAllocationsForField(UUID fieldId);

    List<ConsumptionSummary> getCompletedConsumptions();

    List<ConsumptionSummary> getConsumptionsForField(UUID fieldId);

    record AllocationSummary(
            UUID id,
            UUID fieldId,
            UUID waterSourceId,
            BigDecimal allocatedVolumeM3,
            Instant periodStart,
            Instant periodEnd,
            String status
    ) {}

    record ConsumptionSummary(
            UUID id,
            UUID fieldId,
            BigDecimal actualVolumeM3,
            Instant startedAt,
            Instant endedAt
    ) {}
}
