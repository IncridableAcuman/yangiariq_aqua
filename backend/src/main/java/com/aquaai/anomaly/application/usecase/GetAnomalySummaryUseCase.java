package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalySummaryData;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetAnomalySummaryUseCase {

    private final AnomalyRepository anomalyRepository;
    private final FieldRepository fieldRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public AnomalySummaryData execute() {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        if (currentRole == UserRole.FARMER) {
            List<Field> farmerFields = fieldRepository.findByOwnerId(currentUserId);
            if (farmerFields.isEmpty()) {
                return new AnomalySummaryData(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            }

            Set<UUID> fieldIds = farmerFields.stream().map(Field::getId).collect(Collectors.toSet());
            List<Anomaly> all = anomalyRepository.findAll(null, null, null, null, null, null, null, null, 0, 500);
            List<Anomaly> owned = all.stream().filter(a -> fieldIds.contains(a.getFieldId())).toList();

            long totalActive = owned.stream().filter(a -> a.getStatus().isActive()).count();
            long critical = owned.stream().filter(a -> a.getStatus().isActive() && a.getSeverity() == AnomalySeverity.CRITICAL).count();
            long high = owned.stream().filter(a -> a.getStatus().isActive() && a.getSeverity() == AnomalySeverity.HIGH).count();
            long medium = owned.stream().filter(a -> a.getStatus().isActive() && a.getSeverity() == AnomalySeverity.MEDIUM).count();
            long low = owned.stream().filter(a -> a.getStatus().isActive() && a.getSeverity() == AnomalySeverity.LOW).count();
            long info = owned.stream().filter(a -> a.getStatus().isActive() && a.getSeverity() == AnomalySeverity.INFO).count();

            long detected = owned.stream().filter(a -> a.getStatus() == AnomalyStatus.DETECTED).count();
            long acknowledged = owned.stream().filter(a -> a.getStatus() == AnomalyStatus.ACKNOWLEDGED).count();
            long investigating = owned.stream().filter(a -> a.getStatus() == AnomalyStatus.INVESTIGATING).count();
            long resolved = owned.stream().filter(a -> a.getStatus() == AnomalyStatus.RESOLVED).count();
            long dismissed = owned.stream().filter(a -> a.getStatus() == AnomalyStatus.DISMISSED).count();

            return new AnomalySummaryData(totalActive, critical, high, medium, low, info, detected, acknowledged, investigating, resolved, dismissed);
        }

        // District-wide summary for OPERATOR / ADMIN
        long detected = anomalyRepository.countByStatus(AnomalyStatus.DETECTED);
        long acknowledged = anomalyRepository.countByStatus(AnomalyStatus.ACKNOWLEDGED);
        long investigating = anomalyRepository.countByStatus(AnomalyStatus.INVESTIGATING);
        long resolved = anomalyRepository.countByStatus(AnomalyStatus.RESOLVED);
        long dismissed = anomalyRepository.countByStatus(AnomalyStatus.DISMISSED);

        long totalActive = detected + acknowledged + investigating;
        long critical = anomalyRepository.countBySeverity(AnomalySeverity.CRITICAL);
        long high = anomalyRepository.countBySeverity(AnomalySeverity.HIGH);
        long medium = anomalyRepository.countBySeverity(AnomalySeverity.MEDIUM);
        long low = anomalyRepository.countBySeverity(AnomalySeverity.LOW);
        long info = anomalyRepository.countBySeverity(AnomalySeverity.INFO);

        return new AnomalySummaryData(totalActive, critical, high, medium, low, info, detected, acknowledged, investigating, resolved, dismissed);
    }
}
