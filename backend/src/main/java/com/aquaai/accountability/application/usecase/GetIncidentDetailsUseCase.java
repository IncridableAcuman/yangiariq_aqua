package com.aquaai.accountability.application.usecase;

import com.aquaai.accountability.application.dto.IncidentDetailsDto;
import com.aquaai.accountability.domain.model.CorrectiveAction;
import com.aquaai.accountability.domain.model.IncidentEvidence;
import com.aquaai.accountability.domain.model.IncidentTimelineEntry;
import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.WaterIncident;
import com.aquaai.accountability.domain.port.AccountabilityFieldPort;
import com.aquaai.accountability.domain.port.AccountabilityNetworkPort;
import com.aquaai.accountability.domain.port.CorrectiveActionRepository;
import com.aquaai.accountability.domain.port.IncidentEvidenceRepository;
import com.aquaai.accountability.domain.port.IncidentRepository;
import com.aquaai.accountability.domain.port.IncidentTimelineRepository;
import com.aquaai.accountability.domain.port.IncidentVerificationRepository;
import com.aquaai.accountability.domain.service.IncidentSlaCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetIncidentDetailsUseCase {

    private final IncidentRepository incidentRepository;
    private final IncidentEvidenceRepository evidenceRepository;
    private final CorrectiveActionRepository actionRepository;
    private final IncidentVerificationRepository verificationRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final AccountabilityNetworkPort networkPort;
    private final AccountabilityFieldPort fieldPort;
    private final IncidentSlaCalculator slaCalculator;

    @Transactional(readOnly = true)
    public IncidentDetailsDto execute(UUID incidentId) {
        WaterIncident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new NoSuchElementException("Incident not found: " + incidentId));

        List<IncidentEvidence> evidence = evidenceRepository.findByIncidentId(incidentId);
        List<CorrectiveAction> actions = actionRepository.findByIncidentId(incidentId);
        List<IncidentVerification> verifications = verificationRepository.findByIncidentId(incidentId);
        List<IncidentTimelineEntry> timeline = timelineRepository.findByIncidentIdOrderByTimestampAsc(incidentId);

        String canalName = incident.getCanalId() != null ?
                networkPort.getCanalName(incident.getCanalId()).orElse(null) : null;
        String wuaName = incident.getWuaId() != null ?
                networkPort.getWuaName(incident.getWuaId()).orElse(null) : null;
        String fieldName = incident.getFieldId() != null ?
                fieldPort.getFieldName(incident.getFieldId()).orElse(null) : null;

        boolean isOverdue = slaCalculator.isOverdue(incident.getSlaDeadline(), incident.getStatus(), Instant.now());

        return new IncidentDetailsDto(
                incident,
                evidence,
                actions,
                verifications,
                timeline,
                canalName,
                wuaName,
                fieldName,
                isOverdue
        );
    }
}
