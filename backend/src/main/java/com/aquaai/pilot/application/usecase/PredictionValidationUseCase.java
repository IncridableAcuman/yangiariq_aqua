package com.aquaai.pilot.application.usecase;

import com.aquaai.pilot.application.dto.PilotDtos.EvaluatePredictionRequest;
import com.aquaai.pilot.application.dto.PilotDtos.PredictionValidationResponse;
import com.aquaai.pilot.domain.model.GroundTruthStatus;
import com.aquaai.pilot.domain.model.PredictionEvaluationStatus;
import com.aquaai.pilot.domain.model.PredictionValidationRecord;
import com.aquaai.pilot.domain.repository.GroundTruthValidationRepository;
import com.aquaai.pilot.domain.repository.PilotProgramRepository;
import com.aquaai.pilot.domain.repository.PredictionValidationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class PredictionValidationUseCase {

    private final PredictionValidationRepository predictionValidationRepository;
    private final GroundTruthValidationRepository groundTruthRepository;
    private final PilotProgramRepository pilotProgramRepository;

    public PredictionValidationUseCase(
            PredictionValidationRepository predictionValidationRepository,
            GroundTruthValidationRepository groundTruthRepository,
            PilotProgramRepository pilotProgramRepository) {
        this.predictionValidationRepository = predictionValidationRepository;
        this.groundTruthRepository = groundTruthRepository;
        this.pilotProgramRepository = pilotProgramRepository;
    }

    public PredictionValidationResponse evaluatePrediction(UUID pilotId, EvaluatePredictionRequest req) {
        pilotProgramRepository.findById(pilotId)
                .orElseThrow(() -> new NoSuchElementException("Pilot program not found: " + pilotId));

        if (req.groundTruthId() != null) {
            var gt = groundTruthRepository.findById(req.groundTruthId())
                    .orElseThrow(() -> new NoSuchElementException("Ground truth record not found: " + req.groundTruthId()));

            if (gt.getVerificationStatus() != GroundTruthStatus.VERIFIED) {
                throw new IllegalStateException("Prediction comparison requires VERIFIED ground truth. Current ground truth status is: " + gt.getVerificationStatus());
            }
        }

        var record = PredictionValidationRecord.create(
                pilotId,
                req.fieldId(),
                req.predictionId(),
                req.modelName(),
                req.modelVersion(),
                req.predictedAt(),
                req.predictionHorizonHours(),
                req.predictedValue(),
                req.actualValue(),
                req.groundTruthId()
        );

        var saved = predictionValidationRepository.save(record);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PredictionValidationResponse getValidation(UUID id) {
        return predictionValidationRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Prediction validation record not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<PredictionValidationResponse> listValidations(UUID pilotId, PredictionEvaluationStatus status) {
        List<PredictionValidationRecord> list = (status != null)
                ? predictionValidationRepository.findByPilotIdAndStatus(pilotId, status)
                : predictionValidationRepository.findByPilotId(pilotId);
        return list.stream().map(this::toDto).toList();
    }

    private PredictionValidationResponse toDto(PredictionValidationRecord r) {
        return new PredictionValidationResponse(
                r.getId(),
                r.getPilotId(),
                r.getFieldId(),
                r.getPredictionId(),
                r.getModelName(),
                r.getModelVersion(),
                r.getPredictedAt(),
                r.getPredictionHorizonHours(),
                r.getPredictedValue(),
                r.getActualValue(),
                r.getGroundTruthId(),
                r.getAbsoluteError(),
                r.getRelativeErrorPercentage(),
                r.getEvaluationStatus().name(),
                r.getEvaluatedAt()
        );
    }
}
