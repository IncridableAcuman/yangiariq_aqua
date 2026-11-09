package com.aquaai.pilot.domain.repository;

import com.aquaai.pilot.domain.model.PilotParticipant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PilotParticipantRepository {
    PilotParticipant save(PilotParticipant participant);
    Optional<PilotParticipant> findById(UUID id);
    List<PilotParticipant> findByPilotId(UUID pilotId);
    List<PilotParticipant> findByUserId(UUID userId);
    Optional<PilotParticipant> findByPilotIdAndUserId(UUID pilotId, UUID userId);
    void delete(UUID id);
}
