package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.CreateIncidentCommand;
import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.AccountabilityNotificationPort;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import com.aquaai.accountability.domain.service.IncidentDeduplicationEngine;
import com.aquaai.accountability.domain.service.IncidentSlaCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateIncidentUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentSlaCalculator slaCalculator;
    private final IncidentDeduplicationEngine deduplicationEngine;
    private final AccountabilityNotificationPort notificationPort;

    @Transactional
    public WaterIncident execute(CreateIncidentCommand command) {
        // 1. Idempotency Check: if source entity ID is provided, check existing
        if (command.sourceEntityId() != null) {
            Optional<WaterIncident> existing = incidentRepository.findBySource(
                    command.sourceModule(), command.sourceEntityType(), command.sourceEntityId()
            );
            if (existing.isPresent()) {
                WaterIncident inc = existing.get();
                if (inc.getStatus().isActive()) {
                    log.info("[INCIDENT DEDUP] Reusing existing active incident {} for source {}:{}:{}",
                            inc.getIncidentNumber(), command.sourceModule(), command.sourceEntityType(), command.sourceEntityId());
                    return inc;
                }
            }
        }

        // 2. Compute SLA Deadline
        Instant detectionTime = command.detectionTimestamp() != null ? command.detectionTimestamp() : Instant.now();
        Instant slaDeadline = slaCalculator.calculateDeadline(detectionTime, command.severity());

        // 3. Build Incident Aggregate
        WaterIncident incident = WaterIncident.builder()
                .title(command.title())
                .description(command.description())
                .category(command.category())
                .severity(command.severity())
                .confidence(command.confidence() != null ? command.confidence() : EvidenceConfidence.UNVERIFIED)
                .status(IncidentStatus.DETECTED)
                .sourceModule(command.sourceModule())
                .sourceEntityType(command.sourceEntityType())
                .sourceEntityId(command.sourceEntityId())
                .findingReference(command.findingReference())
                .wuaId(command.wuaId())
                .canalId(command.canalId())
                .fieldId(command.fieldId())
                .locationDescription(command.locationDescription())
                .detectionTimestamp(detectionTime)
                .slaDeadline(slaDeadline)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        WaterIncident saved = incidentRepository.save(incident);

        // 4. Record Timeline Entry
        IncidentTimelineEntry timelineEntry = IncidentTimelineEntry.builder()
                .incidentId(saved.getId())
                .eventType("CREATED")
                .actorId(command.reportedBy())
                .actorName(command.reporterName() != null ? command.reporterName() : "System Detector")
                .actorRole(command.reporterRole() != null ? command.reporterRole() : "SYSTEM")
                .timestamp(Instant.now())
                .previousState(null)
                .newState(saved.getStatus())
                .reason("Incident detected from source " + command.sourceModule() + ":" + command.sourceEntityType())
                .correlationId(UUID.randomUUID().toString())
                .build();
        timelineRepository.save(timelineEntry);

        // 5. Trigger Notification
        notificationPort.sendIncidentAlert(
                saved.getId(),
                saved.getIncidentNumber(),
                saved.getSeverity(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getConfidence() == EvidenceConfidence.SIMULATED
        );

        return saved;
    }
}
