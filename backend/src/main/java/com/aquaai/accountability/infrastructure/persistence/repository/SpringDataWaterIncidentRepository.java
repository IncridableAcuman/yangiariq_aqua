package com.aquaai.accountability.infrastructure.persistence.repository;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentCategory;
import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.infrastructure.persistence.entity.WaterIncidentJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataWaterIncidentRepository extends JpaRepository<WaterIncidentJpaEntity, UUID> {

    Optional<WaterIncidentJpaEntity> findByIncidentNumber(String incidentNumber);

    Optional<WaterIncidentJpaEntity> findBySourceModuleAndSourceEntityTypeAndSourceEntityId(
            String sourceModule,
            String sourceEntityType,
            UUID sourceEntityId
    );

    long countByStatus(IncidentStatus status);

    long countBySeverity(IncidentSeverity severity);

    @Query("SELECT COUNT(i) FROM WaterIncidentJpaEntity i WHERE i.slaDeadline IS NOT NULL AND i.slaDeadline < :now AND i.status NOT IN :terminalStatuses")
    long countOverdue(@Param("now") Instant now, @Param("terminalStatuses") Collection<IncidentStatus> terminalStatuses);

    @Query("SELECT i FROM WaterIncidentJpaEntity i WHERE i.slaDeadline IS NOT NULL AND i.slaDeadline < :now AND i.status NOT IN :terminalStatuses ORDER BY i.slaDeadline ASC")
    List<WaterIncidentJpaEntity> findOverdue(@Param("now") Instant now, @Param("terminalStatuses") Collection<IncidentStatus> terminalStatuses);

    Page<WaterIncidentJpaEntity> findAllByOrderByDetectionTimestampDesc(Pageable pageable);

    @Query(
        value = """
            SELECT i FROM WaterIncidentJpaEntity i
            WHERE (:category IS NULL OR i.category = :category)
              AND (:severity IS NULL OR i.severity = :severity)
              AND (:status IS NULL OR i.status = :status)
              AND (:wuaId IS NULL OR i.wuaId = :wuaId)
              AND (:canalId IS NULL OR i.canalId = :canalId)
              AND (:fieldId IS NULL OR i.fieldId = :fieldId)
              AND (cast(:startDate as timestamp) IS NULL OR i.detectionTimestamp >= :startDate)
              AND (cast(:endDate as timestamp) IS NULL OR i.detectionTimestamp <= :endDate)
              AND (cast(:overdueOnly as boolean) IS NULL OR cast(:overdueOnly as boolean) = FALSE OR (i.slaDeadline IS NOT NULL AND i.slaDeadline < :now AND i.status NOT IN :terminalStatuses))
            ORDER BY i.detectionTimestamp DESC
        """,
        countQuery = """
            SELECT COUNT(i) FROM WaterIncidentJpaEntity i
            WHERE (:category IS NULL OR i.category = :category)
              AND (:severity IS NULL OR i.severity = :severity)
              AND (:status IS NULL OR i.status = :status)
              AND (:wuaId IS NULL OR i.wuaId = :wuaId)
              AND (:canalId IS NULL OR i.canalId = :canalId)
              AND (:fieldId IS NULL OR i.fieldId = :fieldId)
              AND (cast(:startDate as timestamp) IS NULL OR i.detectionTimestamp >= :startDate)
              AND (cast(:endDate as timestamp) IS NULL OR i.detectionTimestamp <= :endDate)
              AND (cast(:overdueOnly as boolean) IS NULL OR cast(:overdueOnly as boolean) = FALSE OR (i.slaDeadline IS NOT NULL AND i.slaDeadline < :now AND i.status NOT IN :terminalStatuses))
        """
    )
    Page<WaterIncidentJpaEntity> filterIncidents(
            @Param("category") IncidentCategory category,
            @Param("severity") IncidentSeverity severity,
            @Param("status") IncidentStatus status,
            @Param("wuaId") UUID wuaId,
            @Param("canalId") UUID canalId,
            @Param("fieldId") UUID fieldId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            @Param("overdueOnly") Boolean overdueOnly,
            @Param("now") Instant now,
            @Param("terminalStatuses") Collection<IncidentStatus> terminalStatuses,
            Pageable pageable
    );
}
