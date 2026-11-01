package com.aquaai.intelligence.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.intelligence.application.dto.TelemetrySignalResponse;
import com.aquaai.intelligence.domain.model.TelemetrySignal;
import com.aquaai.intelligence.domain.port.IntelligenceTelemetryPort;
import com.aquaai.intelligence.domain.service.FreshnessEvaluator;
import com.aquaai.intelligence.domain.service.TelemetryNormalizationService;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetFieldTelemetrySignalsUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final IntelligenceTelemetryPort telemetryPort;

    private final FreshnessEvaluator freshnessEvaluator = new FreshnessEvaluator();
    private final TelemetryNormalizationService normalizationService = new TelemetryNormalizationService(freshnessEvaluator);

    @Transactional(readOnly = true)
    public List<TelemetrySignalResponse> execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        List<SensorReading> readings = telemetryPort.getLatestReadings(fieldId);
        List<TelemetrySignal> signals = normalizationService.normalize(readings, Instant.now());

        return signals.stream().map(TelemetrySignalResponse::fromDomain).toList();
    }
}
