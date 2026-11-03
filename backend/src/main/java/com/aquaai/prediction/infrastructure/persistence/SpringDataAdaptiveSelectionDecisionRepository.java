package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataAdaptiveSelectionDecisionRepository extends JpaRepository<AdaptiveSelectionDecisionJpaEntity, UUID> {

    Optional<AdaptiveSelectionDecisionJpaEntity> findByPredictionId(UUID predictionId);

    List<AdaptiveSelectionDecisionJpaEntity> findByFieldIdOrderByCreatedAtDesc(UUID fieldId, Pageable pageable);

    List<AdaptiveSelectionDecisionJpaEntity> findByFieldIdAndPredictionTypeOrderByCreatedAtDesc(
            UUID fieldId, String predictionType, Pageable pageable);

    @Query("SELECT d FROM AdaptiveSelectionDecisionJpaEntity d ORDER BY d.createdAt DESC")
    List<AdaptiveSelectionDecisionJpaEntity> findRecentDecisions(Pageable pageable);

    long countBySelectionMode(String selectionMode);
}
