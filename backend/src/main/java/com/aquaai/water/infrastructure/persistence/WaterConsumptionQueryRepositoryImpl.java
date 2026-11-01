package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import com.aquaai.water.domain.repository.WaterConsumptionQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Infrastructure implementation of {@link WaterConsumptionQueryRepository}.
 * Executes optimized SQL aggregate queries and grouped lookups in PostgreSQL/H2,
 * completely eliminating N+1 database roundtrips.
 */
@Repository
@RequiredArgsConstructor
public class WaterConsumptionQueryRepositoryImpl implements WaterConsumptionQueryRepository {

    private final SpringDataWaterConsumptionRepository springDataRepository;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getConsumedVolumeByAllocationId(UUID allocationId) {
        if (allocationId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal consumed = springDataRepository.sumConsumedVolumeByAllocationId(
                allocationId,
                IrrigationEventStatus.COMPLETED
        );
        return consumed != null ? consumed : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getConsumedVolumesByAllocationIds(Collection<UUID> allocationIds) {
        if (allocationIds == null || allocationIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = springDataRepository.sumConsumedVolumesByAllocationIds(
                allocationIds,
                IrrigationEventStatus.COMPLETED
        );

        Map<UUID, BigDecimal> result = new HashMap<>();
        for (UUID id : allocationIds) {
            result.put(id, BigDecimal.ZERO);
        }

        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] instanceof UUID allocId && row[1] instanceof BigDecimal volume) {
                    result.put(allocId, volume);
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletedEventsByAllocationId(UUID allocationId) {
        if (allocationId == null) {
            return 0;
        }
        return springDataRepository.countCompletedEventsByAllocationId(
                allocationId,
                IrrigationEventStatus.COMPLETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getConsumedVolumeByPlanId(UUID planId) {
        if (planId == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal consumed = springDataRepository.sumConsumedVolumeByPlanId(
                planId,
                IrrigationEventStatus.COMPLETED
        );
        return consumed != null ? consumed : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> getConsumedVolumesByPlanIds(Collection<UUID> planIds) {
        if (planIds == null || planIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Object[]> rows = springDataRepository.sumConsumedVolumesByPlanIds(
                planIds,
                IrrigationEventStatus.COMPLETED
        );

        Map<UUID, BigDecimal> result = new HashMap<>();
        for (UUID id : planIds) {
            result.put(id, BigDecimal.ZERO);
        }

        if (rows != null) {
            for (Object[] row : rows) {
                if (row != null && row.length >= 2 && row[0] instanceof UUID planId && row[1] instanceof BigDecimal volume) {
                    result.put(planId, volume);
                }
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletedEventsByPlanId(UUID planId) {
        if (planId == null) {
            return 0;
        }
        return springDataRepository.countCompletedEventsByPlanId(
                planId,
                IrrigationEventStatus.COMPLETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FieldWaterSummaryData getFieldWaterSummary(UUID fieldId, Instant from, Instant to) {
        if (fieldId == null) {
            return new FieldWaterSummaryData(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0);
        }

        // 1. Aggregate allocations
        List<Object[]> allocRows = springDataRepository.aggregateAllocationsByFieldAndPeriod(
                fieldId,
                from,
                to,
                WaterAllocationStatus.CANCELLED
        );

        BigDecimal totalAllocated = BigDecimal.ZERO;
        long allocationCount = 0;

        if (allocRows != null && !allocRows.isEmpty()) {
            Object[] row = allocRows.get(0);
            if (row != null && row.length >= 2) {
                if (row[0] instanceof BigDecimal sum) {
                    totalAllocated = sum;
                }
                if (row[1] instanceof Number count) {
                    allocationCount = count.longValue();
                }
            }
        }

        // 2. Aggregate completed events
        List<Object[]> eventRows = springDataRepository.aggregateEventsByFieldAndPeriod(
                fieldId,
                from,
                to,
                IrrigationEventStatus.COMPLETED
        );

        BigDecimal totalConsumed = BigDecimal.ZERO;
        long eventCount = 0;

        if (eventRows != null && !eventRows.isEmpty()) {
            Object[] row = eventRows.get(0);
            if (row != null && row.length >= 2) {
                if (row[0] instanceof BigDecimal sum) {
                    totalConsumed = sum;
                }
                if (row[1] instanceof Number count) {
                    eventCount = count.longValue();
                }
            }
        }

        return new FieldWaterSummaryData(
                totalAllocated,
                allocationCount,
                totalConsumed,
                eventCount
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, FieldWaterSummaryData> getFieldWaterSummaries(Collection<UUID> fieldIds, Instant from, Instant to) {
        if (fieldIds == null || fieldIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, BigDecimal> allocatedMap = new HashMap<>();
        Map<UUID, Long> allocCountMap = new HashMap<>();
        Map<UUID, BigDecimal> consumedMap = new HashMap<>();
        Map<UUID, Long> eventCountMap = new HashMap<>();

        for (UUID fieldId : fieldIds) {
            allocatedMap.put(fieldId, BigDecimal.ZERO);
            allocCountMap.put(fieldId, 0L);
            consumedMap.put(fieldId, BigDecimal.ZERO);
            eventCountMap.put(fieldId, 0L);
        }

        List<Object[]> allocRows = springDataRepository.aggregateAllocationsByFieldIdsAndPeriod(
                fieldIds,
                from,
                to,
                WaterAllocationStatus.CANCELLED
        );
        if (allocRows != null) {
            for (Object[] row : allocRows) {
                if (row != null && row.length >= 3 && row[0] instanceof UUID fId) {
                    if (row[1] instanceof BigDecimal sum) {
                        allocatedMap.put(fId, sum);
                    }
                    if (row[2] instanceof Number count) {
                        allocCountMap.put(fId, count.longValue());
                    }
                }
            }
        }

        List<Object[]> eventRows = springDataRepository.aggregateEventsByFieldIdsAndPeriod(
                fieldIds,
                from,
                to,
                IrrigationEventStatus.COMPLETED
        );
        if (eventRows != null) {
            for (Object[] row : eventRows) {
                if (row != null && row.length >= 3 && row[0] instanceof UUID fId) {
                    if (row[1] instanceof BigDecimal sum) {
                        consumedMap.put(fId, sum);
                    }
                    if (row[2] instanceof Number count) {
                        eventCountMap.put(fId, count.longValue());
                    }
                }
            }
        }

        Map<UUID, FieldWaterSummaryData> result = new HashMap<>();
        for (UUID fieldId : fieldIds) {
            result.put(fieldId, new FieldWaterSummaryData(
                    allocatedMap.get(fieldId),
                    allocCountMap.get(fieldId),
                    consumedMap.get(fieldId),
                    eventCountMap.get(fieldId)
            ));
        }

        return result;
    }
}
