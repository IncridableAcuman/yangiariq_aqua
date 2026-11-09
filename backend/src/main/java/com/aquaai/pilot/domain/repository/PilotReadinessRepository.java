package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PilotReadinessAssessment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PilotReadinessRepository {
    PilotReadinessAssessment save(PilotReadinessAssessment assessment);
    Optional<PilotReadinessAssessment> findById(UUID id);
    Optional<PilotReadinessAssessment> findLatestByPilotId(UUID pilotId);
    List<PilotReadinessAssessment> findByPilotId(UUID pilotId);
}
