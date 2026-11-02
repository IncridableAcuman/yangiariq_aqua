package com.aquaai.prediction.domain.port;

import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound domain port for persisting, querying, and auditing prediction evaluations.
 */
public interface PredictionEvaluationRepository {

    void save(PredictionEvaluation evaluation);

    void saveAll(Collection<PredictionEvaluation> evaluations);

    Optional<PredictionEvaluation> findById(UUID id);

    Optional<PredictionEvaluation> findByPredictionId(UUID predictionId);

    boolean existsByPredictionId(UUID predictionId);

    List<PredictionEvaluation> findEvaluations(
            Collection<UUID> fieldIds,
            PredictionType type,
            EvaluationStatus status,
            Instant from,
            Instant to,
            int page,
            int size
    );

    long countEvaluations(
            Collection<UUID> fieldIds,
            PredictionType type,
            EvaluationStatus status,
            Instant from,
            Instant to
    );

    List<PredictionEvaluation> findEvaluationsForPeriod(
            Collection<UUID> fieldIds,
            Instant from,
            Instant to
    );

    List<PredictionEvaluation> findEvaluationsByField(
            UUID fieldId,
            Instant from,
            Instant to
    );

    List<PredictionEvaluation> findByPredictionTypeAndPeriod(
            PredictionType type,
            Instant from,
            Instant to
    );
}
