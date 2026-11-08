package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.ProposeActionCommand;
import com.aquaai.accountability.domain.model.ActionStatus;
import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.CorrectiveActionRepository;
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
public class ProposeCorrectiveActionUseCase {

    private final IncidentRepository incidentRepository;
    private final CorrectiveActionRepository actionRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public CorrectiveAction execute(ProposeActionCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        CorrectiveAction action = CorrectiveAction.builder()
                .incidentId(incident.getId())
                .title(command.title())
                .description(command.description())
                .objective(command.objective())
                .actionOwnerId(command.actionOwnerId())
                .actionOwnerName(command.actionOwnerName())
                .priority(command.priority())
                .dueDate(command.dueDate())
                .expectedOutcome(command.expectedOutcome())
                .status(ActionStatus.PROPOSED)
                .verificationRequirement(command.verificationRequirement())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        CorrectiveAction saved = actionRepository.save(action);

        IncidentStatus previousStatus = incident.getStatus();
        if (incident.getStatus() == IncidentStatus.OPEN || incident.getStatus() == IncidentStatus.INVESTIGATING) {
            stateMachine.validateTransition(incident.getStatus(), IncidentStatus.ACTION_PROPOSED, "Corrective action proposed: " + saved.getActionNumber());
            incident.transitionTo(IncidentStatus.ACTION_PROPOSED, "Corrective action proposed");
            incidentRepository.save(incident);
        }

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(incident.getId())
                .eventType("ACTION_PROPOSED")
                .actorId(command.proposedBy())
                .actorName(command.proposerName())
                .actorRole(command.proposerRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(incident.getStatus())
                .reason("Proposed corrective action: " + saved.getTitle() + " [" + saved.getActionNumber() + "]")
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"actionId\":\"" + saved.getId() + "\",\"actionNumber\":\"" + saved.getActionNumber() + "\"}")
                .build();
        timelineRepository.save(entry);

        return saved;
    }
}
