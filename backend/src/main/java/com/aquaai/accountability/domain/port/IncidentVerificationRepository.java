package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.IncidentVerification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentVerificationRepository {

    IncidentVerification save(IncidentVerification verification);

    Optional<IncidentVerification> findById(UUID id);

    List<IncidentVerification> findByIncidentId(UUID incidentId);
}
