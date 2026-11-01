package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetAnomalyUseCase {

    private final AnomalyRepository anomalyRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public Anomaly execute(UUID anomalyId) {
        if (anomalyId == null) {
            throw new ResourceNotFoundException("Anomaly", "null");
        }

        Anomaly anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", anomalyId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR: Check that farmer owns the field of this anomaly. If not owned, throw 404
        try {
            fieldOwnershipChecker.getFieldForRead(anomaly.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Anomaly", anomalyId);
        }

        return anomaly;
    }
}
