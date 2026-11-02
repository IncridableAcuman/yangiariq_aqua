package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.PredictionMethodVersion;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionMethodVersionRepository {
    Optional<PredictionMethodVersion> findById(UUID id);
    Optional<PredictionMethodVersion> findByVersionTag(String versionTag);
    Optional<PredictionMethodVersion> findActiveByPredictionType(PredictionType type);
    List<PredictionMethodVersion> findAll();
    List<PredictionMethodVersion> findByPredictionType(PredictionType type);
    PredictionMethodVersion save(PredictionMethodVersion version);
}
