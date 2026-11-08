package com.aquaai.accountability.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable independent verification record assessing whether an incident has been resolved.
 */
@Getter
@Builder(toBuilder = true)
public class IncidentVerification {

    private final UUID id;
    private final UUID incidentId;
    private final UUID actionId;
    private final UUID verifierId;
    private final String verifierName;
    private final Instant verifiedAt;
    private final String verificationMethod;
    private final VerificationOutcome result;
    private final String explanatoryNotes;
    private final String supportingEvidenceUri;

    public IncidentVerification(
            UUID id,
            UUID incidentId,
            UUID actionId,
            UUID verifierId,
            String verifierName,
            Instant verifiedAt,
            String verificationMethod,
            VerificationOutcome result,
            String explanatoryNotes,
            String supportingEvidenceUri
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.incidentId = Objects.requireNonNull(incidentId, "Incident ID cannot be null");
        this.actionId = actionId;
        this.verifierId = Objects.requireNonNull(verifierId, "Verifier ID cannot be null");
        this.verifierName = Objects.requireNonNull(verifierName, "Verifier name cannot be null").trim();
        this.verifiedAt = verifiedAt != null ? verifiedAt : Instant.now();
        this.verificationMethod = Objects.requireNonNull(verificationMethod, "Verification method cannot be null").trim();
        this.result = Objects.requireNonNull(result, "Verification outcome cannot be null");
        this.explanatoryNotes = Objects.requireNonNull(explanatoryNotes, "Explanatory notes cannot be null").trim();
        this.supportingEvidenceUri = supportingEvidenceUri;
    }
}
