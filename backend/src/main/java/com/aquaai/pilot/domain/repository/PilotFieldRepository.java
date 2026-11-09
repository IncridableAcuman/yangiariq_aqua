package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PilotField;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PilotFieldRepository {
    PilotField save(PilotField pilotField);
    Optional<PilotField> findById(UUID id);
    List<PilotField> findByPilotId(UUID pilotId);
    Optional<PilotField> findByPilotIdAndFieldId(UUID pilotId, UUID fieldId);
    void delete(UUID id);
}
