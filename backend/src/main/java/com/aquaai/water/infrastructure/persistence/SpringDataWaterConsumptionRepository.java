package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationEventStatus;
import com.aquaai.water.domain.model.WaterAllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataWaterConsumptionRepository extends JpaRepository<IrrigationEventJpaEntity, UUID> {

    @Query("""
        SELECT COALESCE(SUM(e.actualVolumeM3), 0)
        FROM IrrigationEventJpaEntity e, IrrigationPlanJpaEntity p
        WHERE e.irrigationPlanId = p.id
          AND p.waterAllocationId = :allocationId
          AND e.fieldId = p.fieldId
          AND e.status = :completedStatus
          AND e.actualVolumeM3 IS NOT NULL
    """)
    BigDecimal sumConsumedVolumeByAllocationId(
            @Param("allocationId") UUID allocationId,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT p.waterAllocationId, COALESCE(SUM(e.actualVolumeM3), 0)
        FROM IrrigationEventJpaEntity e, IrrigationPlanJpaEntity p
        WHERE e.irrigationPlanId = p.id
          AND p.waterAllocationId IN :allocationIds
          AND e.fieldId = p.fieldId
          AND e.status = :completedStatus
          AND e.actualVolumeM3 IS NOT NULL
        GROUP BY p.waterAllocationId
    """)
    List<Object[]> sumConsumedVolumesByAllocationIds(
            @Param("allocationIds") Collection<UUID> allocationIds,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT COUNT(e)
        FROM IrrigationEventJpaEntity e, IrrigationPlanJpaEntity p
        WHERE e.irrigationPlanId = p.id
          AND p.waterAllocationId = :allocationId
          AND e.fieldId = p.fieldId
          AND e.status = :completedStatus
    """)
    long countCompletedEventsByAllocationId(
            @Param("allocationId") UUID allocationId,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT COALESCE(SUM(e.actualVolumeM3), 0)
        FROM IrrigationEventJpaEntity e
        WHERE e.irrigationPlanId = :planId
          AND e.status = :completedStatus
          AND e.actualVolumeM3 IS NOT NULL
    """)
    BigDecimal sumConsumedVolumeByPlanId(
            @Param("planId") UUID planId,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT e.irrigationPlanId, COALESCE(SUM(e.actualVolumeM3), 0)
        FROM IrrigationEventJpaEntity e
        WHERE e.irrigationPlanId IN :planIds
          AND e.status = :completedStatus
          AND e.actualVolumeM3 IS NOT NULL
        GROUP BY e.irrigationPlanId
    """)
    List<Object[]> sumConsumedVolumesByPlanIds(
            @Param("planIds") Collection<UUID> planIds,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT COUNT(e)
        FROM IrrigationEventJpaEntity e
        WHERE e.irrigationPlanId = :planId
          AND e.status = :completedStatus
    """)
    long countCompletedEventsByPlanId(
            @Param("planId") UUID planId,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );

    @Query("""
        SELECT COALESCE(SUM(a.allocatedVolumeM3), 0), COUNT(a)
        FROM WaterAllocationJpaEntity a
        WHERE a.fieldId = :fieldId
          AND a.status <> :cancelledStatus
          AND (cast(:from as timestamp) IS NULL OR a.periodEnd >= :from)
          AND (cast(:to as timestamp) IS NULL OR a.periodStart <= :to)
    """)
    List<Object[]> aggregateAllocationsByFieldAndPeriod(
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("cancelledStatus") WaterAllocationStatus cancelledStatus
    );

    @Query("""
        SELECT COALESCE(SUM(e.actualVolumeM3), 0), COUNT(e)
        FROM IrrigationEventJpaEntity e
        WHERE e.fieldId = :fieldId
          AND e.status = :completedStatus
          AND e.actualVolumeM3 IS NOT NULL
          AND (cast(:from as timestamp) IS NULL OR e.startedAt >= :from)
          AND (cast(:to as timestamp) IS NULL OR e.startedAt <= :to)
    """)
    List<Object[]> aggregateEventsByFieldAndPeriod(
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("completedStatus") IrrigationEventStatus completedStatus
    );
}
