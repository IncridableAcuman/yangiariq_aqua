package com.aquaai.prediction.infrastructure.persistence;

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
public interface SpringDataPredictionEvaluationRepository extends JpaRepository<PredictionEvaluationJpaEntity, UUID> {

    Optional<PredictionEvaluationJpaEntity> findByPredictionId(UUID predictionId);

    boolean existsByPredictionId(UUID predictionId);

    @Query("""
        SELECT e FROM PredictionEvaluationJpaEntity e
        WHERE e.fieldId IN :fieldIds
          AND (:predictionType IS NULL OR e.predictionType = :predictionType)
          AND (:evaluationStatus IS NULL OR e.evaluationStatus = :evaluationStatus)
          AND (cast(:from as timestamp) IS NULL OR e.evaluatedAt >= :from)
          AND (cast(:to as timestamp) IS NULL OR e.evaluatedAt <= :to)
        ORDER BY e.evaluatedAt DESC
    """)
    Page<PredictionEvaluationJpaEntity> findFiltered(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("predictionType") String predictionType,
            @Param("evaluationStatus") String evaluationStatus,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
        SELECT COUNT(e) FROM PredictionEvaluationJpaEntity e
        WHERE e.fieldId IN :fieldIds
          AND (:predictionType IS NULL OR e.predictionType = :predictionType)
          AND (:evaluationStatus IS NULL OR e.evaluationStatus = :evaluationStatus)
          AND (cast(:from as timestamp) IS NULL OR e.evaluatedAt >= :from)
          AND (cast(:to as timestamp) IS NULL OR e.evaluatedAt <= :to)
    """)
    long countFiltered(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("predictionType") String predictionType,
            @Param("evaluationStatus") String evaluationStatus,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
        SELECT e FROM PredictionEvaluationJpaEntity e
        WHERE e.fieldId IN :fieldIds
          AND e.evaluatedAt >= :from
          AND e.evaluatedAt <= :to
        ORDER BY e.evaluatedAt DESC
    """)
    List<PredictionEvaluationJpaEntity> findForPeriod(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
        SELECT e FROM PredictionEvaluationJpaEntity e
        WHERE e.fieldId = :fieldId
          AND e.evaluatedAt >= :from
          AND e.evaluatedAt <= :to
        ORDER BY e.evaluatedAt DESC
    """)
    List<PredictionEvaluationJpaEntity> findByFieldForPeriod(
            @Param("fieldId") UUID fieldId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );

    @Query("""
        SELECT e FROM PredictionEvaluationJpaEntity e
        WHERE e.predictionType = :predictionType
          AND (cast(:from as timestamp) IS NULL OR e.targetTime >= :from)
          AND (cast(:to as timestamp) IS NULL OR e.targetTime <= :to)
        ORDER BY e.targetTime ASC
    """)
    List<PredictionEvaluationJpaEntity> findByPredictionTypeAndPeriod(
            @Param("predictionType") String predictionType,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}
