package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.IncidentEvidence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentEvidenceRepository {

    IncidentEvidence save(IncidentEvidence evidence);

    Optional<IncidentEvidence> findById(UUID id);

    List<IncidentEvidence> findByIncidentId(UUID incidentId);
}
