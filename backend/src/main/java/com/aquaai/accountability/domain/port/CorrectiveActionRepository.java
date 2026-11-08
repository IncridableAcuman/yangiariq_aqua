package com.aquaai.accountability.domain.port;

import com.aquaai.accountability.domain.model.CorrectiveAction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CorrectiveActionRepository {

    CorrectiveAction save(CorrectiveAction action);

    Optional<CorrectiveAction> findById(UUID id);

    List<CorrectiveAction> findByIncidentId(UUID incidentId);
}
