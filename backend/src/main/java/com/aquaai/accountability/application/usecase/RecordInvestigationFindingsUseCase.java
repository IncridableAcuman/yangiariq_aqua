package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.RecordInvestigationCommand;
import com.aquaai.accountability.domain.model.IncidentStatus;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordInvestigationFindingsUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;

    @Transactional
    public WaterIncident execute(RecordInvestigationCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        IncidentStatus previousStatus = incident.getStatus();
        incident.recordInvestigation(command.rootCauseCategory(), command.rootCauseDetails(), command.updatedConfidence());
        WaterIncident updated = incidentRepository.save(incident);

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(updated.getId())
                .eventType("INVESTIGATION_UPDATED")
                .actorId(command.investigatorId())
                .actorName(command.investigatorName())
                .actorRole(command.investigatorRole())
                .timestamp(Instant.now())
                .previousState(previousStatus)
                .newState(updated.getStatus())
                .reason(command.notes() != null ? command.notes() : "Updated investigation root cause: " + command.rootCauseCategory())
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"rootCauseCategory\":\"" + command.rootCauseCategory() + "\"}")
                .build();
        timelineRepository.save(entry);

        return updated;
    }
}
