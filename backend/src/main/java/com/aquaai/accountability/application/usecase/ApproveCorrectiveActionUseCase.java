package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.ApproveActionCommand;
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
public class ApproveCorrectiveActionUseCase {

    private final IncidentRepository incidentRepository;
    private final CorrectiveActionRepository actionRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public CorrectiveAction execute(ApproveActionCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        CorrectiveAction action = actionRepository.findById(command.actionId())
                .orElseThrow(() -> new NoSuchElementException("Action not found: " + command.actionId()));

        action.approve(command.approverId(), command.notes());
        CorrectiveAction savedAction = actionRepository.save(action);

        IncidentStatus previousStatus = incident.getStatus();
        if (incident.getStatus() == IncidentStatus.ACTION_PROPOSED) {
            stateMachine.validateTransition(incident.getStatus(), IncidentStatus.ACTION_APPROVED, command.notes());
            incident.transitionTo(IncidentStatus.ACTION_APPROVED, command.notes());
            incidentRepository.save(incident);
        }

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(incident.getId())
                .eventType("ACTION_APPROVED")
                .actorId(command.approverId())
                .actorName(command.approverName())
                .actorRole(command.approverRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(incident.getStatus())
                .reason(command.notes() != null ? command.notes() : "Approved corrective action: " + savedAction.getActionNumber())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"actionId\":\"" + savedAction.getId() + "\"}")
                .build();
        timelineRepository.save(entry);

        return savedAction;
    }
}
