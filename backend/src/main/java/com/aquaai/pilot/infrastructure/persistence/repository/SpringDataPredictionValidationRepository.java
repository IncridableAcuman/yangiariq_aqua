package com.aquaai.pilot.infrastructure.persistence.repository;

import com.aquaai.pilot.infrastructure.persistence.entity.PredictionValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataPredictionValidationRepository extends JpaRepository<PredictionValidationEntity, UUID> {
    List<PredictionValidationEntity> findByPilotIdOrderByEvaluatedAtDesc(UUID pilotId);
    List<PredictionValidationEntity> findByFieldIdOrderByEvaluatedAtDesc(UUID fieldId);
    List<PredictionValidationEntity> findByPilotIdAndEvaluationStatusOrderByEvaluatedAtDesc(UUID pilotId, String evaluationStatus);
    List<PredictionValidationEntity> findByPilotIdAndModelNameOrderByEvaluatedAtDesc(UUID pilotId, String modelName);
    long countByPilotId(UUID pilotId);
}
