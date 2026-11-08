package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.VerifyResolutionCommand;
import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.VerificationOutcome;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.CorrectiveActionRepository;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import com.aquaai.accountability.domain.port.IncidentVerificationRepository;
import com.aquaai.accountability.domain.service.IncidentStateMachine;
import com.aquaai.accountability.domain.service.IndependentVerificationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyResolutionUseCase {

    private final IncidentRepository incidentRepository;
    private final CorrectiveActionRepository actionRepository;
    private final IncidentVerificationRepository verificationRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final IndependentVerificationEngine verificationEngine;
    private final IncidentStateMachine stateMachine;

    @Transactional
    public IncidentVerification execute(VerifyResolutionCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        UUID actionOwnerId = null;
        if (command.actionId() != null) {
            CorrectiveAction action = actionRepository.findById(command.actionId()).orElse(null);
            if (action != null) {
                actionOwnerId = action.getActionOwnerId();
            }
        }

        // 1. Enforce Separation of Duties
        verificationEngine.validateSeparationOfDuties(actionOwnerId, command.verifierId(), command.administrativeOverride());

        // 2. Persist Verification Record
        IncidentVerification verification = IncidentVerification.builder()
                .incidentId(incident.getId())
                .actionId(command.actionId())
                .verifierId(command.verifierId())
                .verifierName(command.verifierName())
                .verifiedAt(Instant.now())
                .verificationMethod(command.verificationMethod())
                .result(command.result())
                .explanatoryNotes(command.explanatoryNotes())
                .supportingEvidenceUri(command.supportingEvidenceUri())
                .build();

        IncidentVerification savedVerification = verificationRepository.save(verification);

        // 3. Determine and Apply Post-Verification Lifecycle State
        IncidentStatus targetStatus = verificationEngine.determinePostVerificationStatus(command.result());
        stateMachine.validateTransition(incident.getStatus(), targetStatus, command.explanatoryNotes());

        IncidentStatus previousStatus = incident.getStatus();
        if (command.result() == VerificationOutcome.VERIFIED_RESOLVED) {
            incident.resolve("Independently verified resolved by " + command.verifierName() + ": " + command.explanatoryNotes());
        } else {
            incident.transitionTo(targetStatus, "Verification result " + command.result() + ": " + command.explanatoryNotes());
        }
        incidentRepository.save(incident);

        // 4. Record Timeline Audit
        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(incident.getId())
                .eventType(command.result() == VerificationOutcome.VERIFIED_RESOLVED ? "RESOLVED" : "VERIFICATION_FAILED")
                .actorId(command.verifierId())
                .actorName(command.verifierName())
                .actorRole(command.verifierRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(incident.getStatus())
                .reason("Verification outcome: " + command.result() + ". Notes: " + command.explanatoryNotes())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"verificationId\":\"" + savedVerification.getId() + "\",\"result\":\"" + command.result() + "\"}")
                .build();
        timelineRepository.save(entry);

        return savedVerification;
    }
}
