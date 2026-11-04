package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.PredictionTrainingDataset;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound domain port for persisting and querying training dataset snapshots.
 */
public interface PredictionTrainingDatasetRepository {

    PredictionTrainingDataset save(PredictionTrainingDataset dataset);

    Optional<PredictionTrainingDataset> findById(UUID id);

    List<PredictionTrainingDataset> findByPredictionType(PredictionType type);

    List<PredictionTrainingDataset> findAll();

    Optional<PredictionTrainingDataset> findLatestByPredictionType(PredictionType type);

    Optional<PredictionTrainingDataset> findByDatasetHash(String hash);
}
