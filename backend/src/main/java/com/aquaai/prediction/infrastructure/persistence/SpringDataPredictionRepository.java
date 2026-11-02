package com.aquaai.prediction.infrastructure.persistence;

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
public interface SpringDataPredictionRepository extends JpaRepository<PredictionJpaEntity, UUID> {

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.fieldId = :fieldId
          AND p.predictionType = :predictionType
          AND p.horizon = :horizon
        ORDER BY p.generatedAt DESC
        LIMIT 1
    """)
    Optional<PredictionJpaEntity> findLatestByFieldAndTypeAndHorizon(
            @Param("fieldId") UUID fieldId,
            @Param("predictionType") String predictionType,
            @Param("horizon") String horizon
    );

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.fieldId = :fieldId
          AND p.horizon = :horizon
        ORDER BY p.generatedAt DESC
    """)
    List<PredictionJpaEntity> findByFieldIdAndHorizon(
            @Param("fieldId") UUID fieldId,
            @Param("horizon") String horizon
    );

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.fieldId IN :fieldIds
          AND p.horizon = :horizon
        ORDER BY p.generatedAt DESC
    """)
    List<PredictionJpaEntity> findByFieldIdsAndHorizon(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("horizon") String horizon
    );

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.fieldId IN :fieldIds
          AND p.horizon = :horizon
          AND p.riskLevel IN :riskLevels
        ORDER BY p.generatedAt DESC
    """)
    List<PredictionJpaEntity> findEmergingRisks(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("horizon") String horizon,
            @Param("riskLevels") Collection<String> riskLevels
    );

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.fieldId IN :fieldIds
          AND p.generatedAt BETWEEN :from AND :to
        ORDER BY p.generatedAt DESC
    """)
    List<PredictionJpaEntity> findForecastHistory(
            @Param("fieldIds") Collection<UUID> fieldIds,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM PredictionJpaEntity p
        WHERE p.accuracyStatus = 'PENDING'
          AND p.targetTime <= :cutoff
        ORDER BY p.targetTime ASC
    """)
    List<PredictionJpaEntity> findPendingEvaluations(@Param("cutoff") Instant cutoff);
}
