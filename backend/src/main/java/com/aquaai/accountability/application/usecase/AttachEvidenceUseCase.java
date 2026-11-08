package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.AttachEvidenceCommand;
import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.IncidentEvidenceRepository;
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
public class AttachEvidenceUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentEvidenceRepository evidenceRepository;
    private final IncidentTimelineRepository timelineRepository;

    @Transactional
    public IncidentEvidence execute(AttachEvidenceCommand command) {
        WaterIncident incident = incidentRepository.findById(command.incidentId())
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + command.incidentId()));

        IncidentEvidence evidence = IncidentEvidence.builder()
                .incidentId(incident.getId())
                .evidenceType(command.evidenceType())
                .title(command.title())
                .description(command.description())
                .evidenceUri(command.evidenceUri())
                .confidence(command.confidence())
                .isSimulated(command.isSimulated())
                .sourceReference(command.sourceReference())
                .recordedBy(command.recordedBy())
                .recordedAt(Instant.now())
                .build();

        IncidentEvidence saved = evidenceRepository.save(evidence);

        IncidentTimelineEntry entry = IncidentTimelineEntry.builder()
                .incidentId(incident.getId())
                .eventType("EVIDENCE_ATTACHED")
                .actorId(command.recordedBy())
                .actorName(command.recorderName())
                .actorRole(command.recorderRole())
                .timestamp(Instant.now())
                .previousState(incident.getStatus())
                .newState(incident.getStatus())
                .reason("Attached evidence: " + command.title() + " (" + command.evidenceType() + ")")
                .correlationId(UUID.randomUUID().toString())
                .metadataJson("{\"evidenceId\":\"" + saved.getId() + "\",\"confidence\":\"" + saved.getConfidence() + "\"}")
                .build();
        timelineRepository.save(entry);

        return saved;
    }
}
