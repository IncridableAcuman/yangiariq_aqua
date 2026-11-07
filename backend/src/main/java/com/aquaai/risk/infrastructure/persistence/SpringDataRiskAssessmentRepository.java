package com.aquaai.risk.infrastructure.persistence;

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
public interface SpringDataRiskAssessmentRepository extends JpaRepository<RiskAssessmentJpaEntity, UUID> {

    @Query("SELECT r FROM RiskAssessmentJpaEntity r WHERE r.fieldId = :fieldId ORDER BY r.evaluatedAt DESC LIMIT 1")
    Optional<RiskAssessmentJpaEntity> findLatestByFieldId(@Param("fieldId") UUID fieldId);

    @Query(value = """
            SELECT DISTINCT ON (field_id) *
            FROM farm_risk_assessments
            WHERE field_id IN (:fieldIds)
            ORDER BY field_id, evaluated_at DESC
            """, nativeQuery = true)
    List<RiskAssessmentJpaEntity> findAllLatestByFieldIds(@Param("fieldIds") Collection<UUID> fieldIds);

    @Query("""
            SELECT r FROM RiskAssessmentJpaEntity r
            WHERE r.fieldId = :fieldId
              AND (:from IS NULL OR r.evaluatedAt >= :from)
              AND (:to IS NULL OR r.evaluatedAt <= :to)
            ORDER BY r.evaluatedAt DESC
            """)
    List<RiskAssessmentJpaEntity> findHistorical(
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(r) FROM RiskAssessmentJpaEntity r
            WHERE r.fieldId = :fieldId
              AND (:from IS NULL OR r.evaluatedAt >= :from)
              AND (:to IS NULL OR r.evaluatedAt <= :to)
            """)
    long countHistorical(
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("SELECT COUNT(r) FROM RiskAssessmentJpaEntity r WHERE r.overallSeverity = :severity AND r.fieldId IN (:fieldIds)")
    long countBySeverityAndFieldIds(@Param("severity") String severity, @Param("fieldIds") Collection<UUID> fieldIds);

    @Query("SELECT COUNT(r) FROM RiskAssessmentJpaEntity r WHERE r.lifecycleStatus = :status AND r.fieldId IN (:fieldIds)")
    long countByLifecycleStatusAndFieldIds(@Param("status") String status, @Param("fieldIds") Collection<UUID> fieldIds);
}
