package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.ReopenIncidentCommand;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import com.aquaai.accountability.domain.service.IncidentStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReopenIncidentUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public WaterIncident execute(ReopenIncidentCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        stateMachine.validateTransition(incident.getStatus(), IncidentStatus.REOPENED, command.reason());

        IncidentStatus previousStatus = incident.getStatus();
        incident.reopen(command.reason());
        WaterIncident updated = incidentRepository.save(incident);

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(updated.getId())
                .eventType("REOPENED")
                .actorId(command.reopenedBy())
                .actorName(command.reopenerName())
                .actorRole(command.reopenerRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(updated.getStatus())
                .reason(command.reason())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"recurrenceCount\":" + updated.getRecurrenceCount() + "}")
                .build();
        timelineRepository.save(entry);

        return updated;
    }
}
