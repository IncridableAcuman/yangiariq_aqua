package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.AdaptiveSelectionDecision;
import com.aquaai.prediction.domain.model.ContextMatchLevel;
import com.aquaai.prediction.domain.model.PredictionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for persisting and querying adaptive selection decisions.
 */
public interface AdaptiveSelectionDecisionRepository {

    AdaptiveSelectionDecision save(AdaptiveSelectionDecision decision);

    Optional<AdaptiveSelectionDecision> findById(UUID id);

    Optional<AdaptiveSelectionDecision> findByPredictionId(UUID predictionId);

    List<AdaptiveSelectionDecision> findByFieldId(UUID fieldId, int limit);

    List<AdaptiveSelectionDecision> findRecent(int limit);

    List<AdaptiveSelectionDecision> findByFieldIdAndType(UUID fieldId, PredictionType type, int limit);

    long countBySelectionMode(ContextMatchLevel mode);

    long countTotal();
}
