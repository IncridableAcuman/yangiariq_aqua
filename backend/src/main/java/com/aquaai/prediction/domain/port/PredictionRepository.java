package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound domain port for prediction persistence, caching, and forecast history retrieval.
 */
public interface PredictionRepository {

    void save(Prediction prediction);

    void saveAll(Collection<Prediction> predictions);

    Optional<Prediction> findById(UUID id);

    Optional<Prediction> findLatestByFieldAndTypeAndHorizon(
            UUID fieldId,
            PredictionType type,
            PredictionHorizon horizon
    );

    List<Prediction> findActivePredictionsForField(UUID fieldId, PredictionHorizon horizon);

    List<Prediction> findActivePredictionsForFields(
            Collection<UUID> fieldIds,
            PredictionHorizon horizon
    );

    List<Prediction> findEmergingRisks(
            Collection<UUID> fieldIds,
            PredictionHorizon horizon,
            List<RiskLevel> elevatedRiskLevels
    );

    List<Prediction> findForecastHistory(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to,
            int limit
    );

    List<Prediction> findPendingEvaluations(Instant evaluationCutoff);
}
