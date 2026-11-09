package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.GroundTruthMeasurement;
import com.aquaai.pilot.domain.model.GroundTruthStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroundTruthValidationRepository {
    GroundTruthMeasurement save(GroundTruthMeasurement measurement);
    Optional<GroundTruthMeasurement> findById(UUID id);
    List<GroundTruthMeasurement> findByPilotId(UUID pilotId);
    List<GroundTruthMeasurement> findByFieldId(UUID fieldId);
    List<GroundTruthMeasurement> findByPilotIdAndStatus(UUID pilotId, GroundTruthStatus status);
    List<GroundTruthMeasurement> findByFieldIdAndMeasurementType(UUID fieldId, String measurementType);
    long countByPilotId(UUID pilotId);
    long countByPilotIdAndStatus(UUID pilotId, GroundTruthStatus status);
}
