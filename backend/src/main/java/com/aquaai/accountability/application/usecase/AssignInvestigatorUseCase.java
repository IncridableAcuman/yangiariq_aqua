package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.AssignInvestigatorCommand;
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
public class AssignInvestigatorUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public WaterIncident execute(AssignInvestigatorCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        IncidentStatus targetStatus = incident.getStatus() == IncidentStatus.OPEN ?
                IncidentStatus.INVESTIGATING : incident.getStatus();
        stateMachine.validateTransition(incident.getStatus(), targetStatus, command.reason());

        IncidentStatus previousStatus = incident.getStatus();
        incident.assignInvestigator(command.investigatorId(), command.investigatorName(), command.organization());
        WaterIncident updated = incidentRepository.save(incident);

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(updated.getId())
                .eventType("ASSIGNED")
                .actorId(command.assignedBy())
                .actorName(command.assignerName())
                .actorRole(command.assignerRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(updated.getStatus())
                .reason(command.reason() != null ? command.reason() : "Assigned investigator: " + command.investigatorName())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"investigatorId\":\"" + command.investigatorId() + "\",\"organization\":\"" + command.organization() + "\"}")
                .build();
        timelineRepository.save(entry);

        return updated;
    }
}
