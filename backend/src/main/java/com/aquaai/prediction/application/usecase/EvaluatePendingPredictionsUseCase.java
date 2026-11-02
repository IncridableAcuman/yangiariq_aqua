package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.EvaluateBatchResponse;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.prediction.domain.service.PredictionOutcomeMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Application service for executing bounded, idempotent evaluation of pending predictions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluatePendingPredictionsUseCase {

    private final PredictionRepository predictionRepository;
    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final PredictionOutcomeMatcher outcomeMatcher = new PredictionOutcomeMatcher();

    private static final int BATCH_LIMIT = 100;

    @Transactional
    public EvaluateBatchResponse execute() {
        return execute(Instant.now(), BATCH_LIMIT);
    }

    @Transactional
    public EvaluateBatchResponse execute(Instant cutoff, int limit) {
        Instant safeCutoff = cutoff != null ? cutoff : Instant.now();
        List<Prediction> pending = predictionRepository.findPendingEvaluations(safeCutoff);

        if (pending.isEmpty()) {
            return EvaluateBatchResponse.builder()
                    .processedCount(0)
                    .evaluatedCount(0)
                    .insufficientDataCount(0)
                    .interruptedCount(0)
                    .message("No pending predictions awaiting evaluation.")
                    .build();
        }

        List<Prediction> batch = pending.stream().limit(Math.max(1, limit)).toList();
        int evaluatedCount = 0;
        int insufficientCount = 0;
        int interruptedCount = 0;

        for (Prediction p : batch) {
            // Idempotency check: if evaluation already exists, skip duplicate persistence
            if (evaluationRepository.existsByPredictionId(p.getId())) {
                continue;
            }

            try {
                PredictionEvaluation eval = outcomeMatcher.matchAndEvaluate(p, dataProvider);

                evaluationRepository.save(eval);

                // Update prediction with recorded outcome and status
                Prediction updated = p.recordOutcome(
                        eval.getActualValue(),
                        eval.getActualObservedAt(),
                        p.getLowerBound() != null && p.getUpperBound() != null
                                ? p.getUpperBound().subtract(p.getLowerBound()).divide(java.math.BigDecimal.valueOf(2), java.math.RoundingMode.HALF_UP)
                                : java.math.BigDecimal.valueOf(5.0)
                );
                predictionRepository.save(updated);

                if (eval.getEvaluationStatus() == EvaluationStatus.EVALUATED) {
                    evaluatedCount++;
                } else if (eval.getEvaluationStatus() == EvaluationStatus.INSUFFICIENT_DATA) {
                    insufficientCount++;
                } else if (eval.getEvaluationStatus() == EvaluationStatus.OUTCOME_INTERRUPTED) {
                    interruptedCount++;
                }
            } catch (Exception e) {
                log.warn("Error evaluating prediction {}: {}", p.getId(), e.getMessage());
            }
        }

        return EvaluateBatchResponse.builder()
                .processedCount(batch.size())
                .evaluatedCount(evaluatedCount)
                .insufficientDataCount(insufficientCount)
                .interruptedCount(interruptedCount)
                .message(String.format("Processed %d predictions: %d evaluated, %d insufficient data, %d interrupted.",
                        batch.size(), evaluatedCount, insufficientCount, interruptedCount))
                .build();
    }
}
