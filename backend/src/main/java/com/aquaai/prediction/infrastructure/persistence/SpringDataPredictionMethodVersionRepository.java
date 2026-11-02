package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPredictionMethodVersionRepository extends JpaRepository<PredictionMethodVersionJpaEntity, UUID> {
    Optional<PredictionMethodVersionJpaEntity> findByVersionTag(String versionTag);
    Optional<PredictionMethodVersionJpaEntity> findByPredictionTypeAndStatus(String predictionType, String status);
    List<PredictionMethodVersionJpaEntity> findByPredictionType(String predictionType);
}
