package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PredictionEvaluationStatus;
import com.aquaai.pilot.domain.model.PredictionValidationRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PredictionValidationRepository {
    PredictionValidationRecord save(PredictionValidationRecord record);
    Optional<PredictionValidationRecord> findById(UUID id);
    List<PredictionValidationRecord> findByPilotId(UUID pilotId);
    List<PredictionValidationRecord> findByFieldId(UUID fieldId);
    List<PredictionValidationRecord> findByPilotIdAndStatus(UUID pilotId, PredictionEvaluationStatus status);
    List<PredictionValidationRecord> findByPilotIdAndModelName(UUID pilotId, String modelName);
    long countByPilotId(UUID pilotId);
}
