package com.aquaai.accountability.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable evidence record supporting an incident finding, investigation, or verification.
 */
@Getter
@Builder(toBuilder = true)
public class IncidentEvidence {

    private final UUID id;
    private final UUID incidentId;
    private final String evidenceType;
    private final String title;
    private final String description;
    private final String evidenceUri;
    private final EvidenceConfidence confidence;
    private final boolean isSimulated;
    private final String sourceReference;
    private final UUID recordedBy;
    private final Instant recordedAt;

    public IncidentEvidence(
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
        this.id = id != null ? id : UUID.randomUUID();
        this.incidentId = Objects.requireNonNull(incidentId, "Incident ID cannot be null");
        this.evidenceType = Objects.requireNonNull(evidenceType, "Evidence type cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null").trim();
        this.description = description;
        this.evidenceUri = evidenceUri;
        this.confidence = confidence != null ? confidence : EvidenceConfidence.UNVERIFIED;
        this.isSimulated = isSimulated;
        this.sourceReference = sourceReference;
        this.recordedBy = recordedBy;
        this.recordedAt = recordedAt != null ? recordedAt : Instant.now();
    }
}
