package com.aquaai.integration.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.integration.application.dto.IngestBatchCommand;
import com.aquaai.integration.application.dto.IngestTelemetryItemCommand;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.application.dto.RecordManualMeasurementCommand;
import com.aquaai.integration.application.service.IngestionPipelineService;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordManualMeasurementUseCase {

    private final IngestionPipelineService ingestionPipelineService;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public IngestionResultDto execute(RecordManualMeasurementCommand command) {
        if (command == null || command.fieldId() == null) {
            throw new BusinessRuleException("Field ID is mandatory for manual measurement", "MISSING_FIELD_ID");
        }

        UUID userId = currentUserProvider.getRequiredUserId();
        UserRole role = currentUserProvider.getRequiredUserRole();

        // Enforce anti-IDOR check on parcel mutation
        fieldOwnershipChecker.getFieldForMutation(command.fieldId(), userId, role);

        String sourceCode = (role == UserRole.FARMER) ? "FARMER_MANUAL_INPUT" : "OPERATOR_MANUAL_INPUT";
        String evidenceClass = (role == UserRole.FARMER)
                ? "UNVERIFIED"
                : (command.verified() ? "VERIFIED" : "OBSERVED");

        String recordId = "MANUAL-" + UUID.randomUUID().toString().substring(0, 8);

        IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                recordId,
                command.fieldId(),
                command.sensorId(),
                null,
                command.sensorType(),
                command.value(),
                command.unit(),
                command.measuredAt(),
                evidenceClass
        );

        IngestBatchCommand batchCommand = new IngestBatchCommand(sourceCode, List.of(item));
        return ingestionPipelineService.processBatch(batchCommand);
    }
}
