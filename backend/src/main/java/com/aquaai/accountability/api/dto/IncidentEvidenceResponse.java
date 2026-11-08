package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import com.aquaai.accountability.domain.model.IncidentEvidence;

import java.time.Instant;
import java.util.UUID;

public record IncidentEvidenceResponse(
        UUID id,
        UUID incidentId,
        String evidenceType,
        String title,
        String description,
        String evidenceUri,
        EvidenceConfidence confidence,
        boolean isSimulated,
        String sourceReference,
        UUID recordedBy,
        Instant recordedAt
) {
    public static IncidentEvidenceResponse from(IncidentEvidence e) {
        if (e == null) return null;
        return new IncidentEvidenceResponse(
                e.getId(),
                e.getIncidentId(),
                e.getEvidenceType(),
                e.getTitle(),
                e.getDescription(),
                e.getEvidenceUri(),
                e.getConfidence(),
                e.isSimulated(),
                e.getSourceReference(),
                e.getRecordedBy(),
                e.getRecordedAt()
        );
    }
}
