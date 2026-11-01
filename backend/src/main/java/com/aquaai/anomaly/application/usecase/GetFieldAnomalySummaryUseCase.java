package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldAnomalySummaryUseCase {

    private final AnomalyRepository anomalyRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    public record FieldAnomalySummaryResult(
            UUID fieldId,
            long activeCount,
            boolean hasCritical,
            boolean hasHigh,
            List<Anomaly> activeAnomalies
    ) {
    }

    @Transactional(readOnly = true)
    public FieldAnomalySummaryResult execute(UUID fieldId) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR check
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        List<Anomaly> all = anomalyRepository.findAll(fieldId, null, null, null, null, null, null, null, 0, 50);
        List<Anomaly> active = all.stream().filter(a -> a.getStatus().isActive()).toList();

        boolean hasCritical = active.stream().anyMatch(a -> a.getSeverity() == AnomalySeverity.CRITICAL);
        boolean hasHigh = active.stream().anyMatch(a -> a.getSeverity() == AnomalySeverity.HIGH);

        return new FieldAnomalySummaryResult(fieldId, active.size(), hasCritical, hasHigh, active);
    }
}
