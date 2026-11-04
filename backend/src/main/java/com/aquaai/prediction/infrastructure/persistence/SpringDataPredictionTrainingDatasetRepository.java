package com.aquaai.prediction.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataPredictionTrainingDatasetRepository extends JpaRepository<PredictionTrainingDatasetJpaEntity, UUID> {

    List<PredictionTrainingDatasetJpaEntity> findByPredictionTypeOrderByCreatedAtDesc(String predictionType);

    Optional<PredictionTrainingDatasetJpaEntity> findFirstByPredictionTypeOrderByCreatedAtDesc(String predictionType);

    Optional<PredictionTrainingDatasetJpaEntity> findByDatasetHash(String datasetHash);
}
