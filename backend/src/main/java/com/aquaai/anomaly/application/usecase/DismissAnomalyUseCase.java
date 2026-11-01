package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DismissAnomalyUseCase {

    private final AnomalyRepository anomalyRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public Anomaly execute(UUID anomalyId, String notes) {
        if (anomalyId == null) {
            throw new ResourceNotFoundException("Anomaly", "null");
        }

        Anomaly anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", anomalyId));

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        try {
            fieldOwnershipChecker.getFieldForRead(anomaly.getFieldId(), currentUserId, currentRole);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Anomaly", anomalyId);
        }

        anomaly.dismiss(currentUserId, notes, Instant.now());
        Anomaly saved = anomalyRepository.save(anomaly);
        log.info("Anomaly dismissed: id={}, fieldId={}, by={}", saved.getId(), saved.getFieldId(), currentUserId);
        return saved;
    }
}
