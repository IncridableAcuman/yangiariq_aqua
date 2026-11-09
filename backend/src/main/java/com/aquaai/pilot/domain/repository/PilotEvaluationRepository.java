package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PilotEvaluationSnapshot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PilotEvaluationRepository {
    PilotEvaluationSnapshot save(PilotEvaluationSnapshot snapshot);
    Optional<PilotEvaluationSnapshot> findById(UUID id);
    Optional<PilotEvaluationSnapshot> findLatestByPilotId(UUID pilotId);
    List<PilotEvaluationSnapshot> findByPilotId(UUID pilotId);
}
