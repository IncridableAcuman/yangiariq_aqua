package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionEvaluationDetailDto;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.prediction.domain.service.PredictionOutcomeMatcher;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPredictionDetailEvaluationUseCase {

    private final PredictionRepository predictionRepository;
    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;
    private final PredictionOutcomeMatcher outcomeMatcher = new PredictionOutcomeMatcher();

    @Transactional
    public PredictionEvaluationDetailDto execute(UUID predictionId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(predictionId, currentUserId, currentRole);
    }

    @Transactional
    public PredictionEvaluationDetailDto execute(
            UUID predictionId,
            UUID currentUserId,
            UserRole currentRole
    ) {
        if (predictionId == null) {
            throw new ResourceNotFoundException("Prediction", "null");
        }

        Prediction prediction = predictionRepository.findById(predictionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prediction", predictionId));

        FieldMetadata field = dataProvider.getFieldMetadata(prediction.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field", prediction.getFieldId()));

        // Anti-IDOR check: FARMER cannot access foreign prediction
        if (currentRole == UserRole.FARMER) {
            if (field.ownerId() == null || !field.ownerId().equals(currentUserId)) {
                throw new ResourceNotFoundException("Prediction", predictionId);
            }
        }

        Optional<PredictionEvaluation> existingEval = evaluationRepository.findByPredictionId(predictionId);
        PredictionEvaluation evaluation;

        if (existingEval.isPresent()) {
            evaluation = existingEval.get();
        } else {
            // Evaluate on demand
            evaluation = outcomeMatcher.matchAndEvaluate(prediction, dataProvider);
            if (prediction.getTargetTime().isBefore(Instant.now())) {
                evaluationRepository.save(evaluation);
                predictionRepository.save(prediction.recordOutcome(
                        evaluation.getActualValue(),
                        evaluation.getActualObservedAt(),
                        null
                ));
            }
        }

        return PredictionEvaluationDetailDto.fromDomain(evaluation, field.name());
    }
}
