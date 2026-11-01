package com.aquaai.anomaly.application.usecase;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.anomaly.domain.repository.AnomalyRepository;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListAnomaliesUseCase {

    private final AnomalyRepository anomalyRepository;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final FieldRepository fieldRepository;
    private final CurrentUserProvider currentUserProvider;

    public record Query(
            UUID fieldId,
            UUID sensorId,
            AnomalyStatus status,
            AnomalySeverity severity,
            AnomalyType anomalyType,
            SensorType sensorType,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
    }

    public record Result(
            List<Anomaly> content,
            long totalElements,
            int page,
            int size
    ) {
    }

    @Transactional(readOnly = true)
    public Result execute(Query query) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // 1. Farmer Anti-IDOR Enforcement
        if (currentRole == UserRole.FARMER) {
            if (query.fieldId() != null) {
                // Farmer specified fieldId: verify ownership, throw 404 if not owned
                fieldOwnershipChecker.getFieldForRead(query.fieldId(), currentUserId, currentRole);
            } else {
                // Farmer did not specify fieldId: find all fields belonging to this farmer
                List<Field> farmerFields = fieldRepository.findByOwnerId(currentUserId);
                if (farmerFields.isEmpty()) {
                    return new Result(Collections.emptyList(), 0, query.page(), query.size());
                }

                Set<UUID> farmerFieldIds = farmerFields.stream().map(Field::getId).collect(Collectors.toSet());
                // Fetch candidate anomalies
                List<Anomaly> allAnomalies = anomalyRepository.findAll(
                        null, query.sensorId(), query.status(), query.severity(),
                        query.anomalyType(), query.sensorType(), query.from(), query.to(),
                        0, 500
                );

                List<Anomaly> filtered = allAnomalies.stream()
                        .filter(a -> farmerFieldIds.contains(a.getFieldId()))
                        .toList();

                int fromIndex = Math.min(query.page() * query.size(), filtered.size());
                int toIndex = Math.min(fromIndex + query.size(), filtered.size());
                List<Anomaly> paged = filtered.subList(fromIndex, toIndex);

                return new Result(paged, filtered.size(), query.page(), query.size());
            }
        }

        // Operator / Admin or Farmer with verified single field
        List<Anomaly> content = anomalyRepository.findAll(
                query.fieldId(), query.sensorId(), query.status(), query.severity(),
                query.anomalyType(), query.sensorType(), query.from(), query.to(),
                query.page(), query.size()
        );

        long total = anomalyRepository.countAll(
                query.fieldId(), query.sensorId(), query.status(), query.severity(),
                query.anomalyType(), query.sensorType(), query.from(), query.to()
        );

        return new Result(content, total, query.page(), query.size());
    }
}
