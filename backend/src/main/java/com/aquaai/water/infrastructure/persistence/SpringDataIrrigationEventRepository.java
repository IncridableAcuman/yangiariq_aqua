package com.aquaai.water.infrastructure.persistence;

import com.aquaai.water.domain.model.IrrigationEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIrrigationEventRepository extends JpaRepository<IrrigationEventJpaEntity, UUID> {

    List<IrrigationEventJpaEntity> findByFieldId(UUID fieldId);

    List<IrrigationEventJpaEntity> findByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status);

    List<IrrigationEventJpaEntity> findByIrrigationPlanId(UUID irrigationPlanId);

    List<IrrigationEventJpaEntity> findByFieldIdAndStartedAtBetween(UUID fieldId, Instant start, Instant end);

    long countByFieldIdAndStatus(UUID fieldId, IrrigationEventStatus status);

    @Query("SELECT COALESCE(SUM(e.actualVolumeM3), 0) FROM IrrigationEventJpaEntity e WHERE e.fieldId = :fieldId AND e.status = :status")
    BigDecimal sumActualVolumeByFieldIdAndStatus(@Param("fieldId") UUID fieldId, @Param("status") IrrigationEventStatus status);

    Optional<IrrigationEventJpaEntity> findByIdAndFieldIdIn(UUID id, java.util.Collection<UUID> fieldIds);

    @Query("""
        SELECT e FROM IrrigationEventJpaEntity e
        WHERE (:fieldIdsEmpty = true OR e.fieldId IN :fieldIds)
          AND (:irrigationPlanId IS NULL OR e.irrigationPlanId = :irrigationPlanId)
          AND (:status IS NULL OR e.status = :status)
          AND (cast(:startDate as timestamp) IS NULL OR (e.endedAt IS NOT NULL AND e.endedAt >= :startDate) OR e.startedAt >= :startDate)
          AND (cast(:endDate as timestamp) IS NULL OR e.startedAt <= :endDate)
    """)
    org.springframework.data.domain.Page<IrrigationEventJpaEntity> findWithFilters(
            @Param("fieldIds") java.util.Collection<UUID> fieldIds,
            @Param("fieldIdsEmpty") boolean fieldIdsEmpty,
            @Param("irrigationPlanId") UUID irrigationPlanId,
            @Param("status") IrrigationEventStatus status,
            @Param("startDate") java.time.Instant startDate,
            @Param("endDate") java.time.Instant endDate,
            org.springframework.data.domain.Pageable pageable
    );
}
