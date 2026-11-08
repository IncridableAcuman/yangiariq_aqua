package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.CompleteActionCommand;
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
public class CompleteCorrectiveActionUseCase {

    private final IncidentRepository incidentRepository;
    private final CorrectiveActionRepository actionRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public CorrectiveAction execute(CompleteActionCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        CorrectiveAction action = actionRepository.findById(command.actionId())
                .orElseThrow(() -> new NoSuchElementException("Action not found: " + command.actionId()));

        action.complete(command.completedBy(), command.completionNotes());
        CorrectiveAction savedAction = actionRepository.save(action);

        IncidentStatus previousStatus = incident.getStatus();
        // Transition incident to PENDING_VERIFICATION (never auto-resolve!)
        if (incident.getStatus() == IncidentStatus.ACTION_APPROVED ||
                incident.getStatus() == IncidentStatus.ACTION_IN_PROGRESS ||
                incident.getStatus() == IncidentStatus.ACTION_PROPOSED) {
            incident.transitionTo(IncidentStatus.PENDING_VERIFICATION, "Action completed. Pending independent verification.");
            incidentRepository.save(incident);
        }

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(incident.getId())
                .eventType("ACTION_COMPLETED")
                .actorId(command.completedBy())
                .actorName(command.completerName())
                .actorRole(command.completerRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(incident.getStatus())
                .reason("Completed action: " + savedAction.getActionNumber() + ". Notes: " + command.completionNotes())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"actionId\":\"" + savedAction.getId() + "\"}")
                .build();
        timelineRepository.save(entry);

        return savedAction;
    }
}
