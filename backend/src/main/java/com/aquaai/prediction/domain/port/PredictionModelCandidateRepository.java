package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound domain port for persisting and querying governed ML model candidates.
 */
public interface PredictionModelCandidateRepository {

    PredictionModelCandidate save(PredictionModelCandidate candidate);

    Optional<PredictionModelCandidate> findById(UUID id);

    Optional<PredictionModelCandidate> findByVersionTag(String versionTag);

    List<PredictionModelCandidate> findByPredictionType(PredictionType type);

    List<PredictionModelCandidate> findAll();

    Optional<PredictionModelCandidate> findActiveByPredictionType(PredictionType type);
}
