package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.IncidentVerification;
import com.aquaai.accountability.domain.model.VerificationOutcome;

import java.time.Instant;
import java.util.UUID;

public record IncidentVerificationResponse(
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
    public static IncidentVerificationResponse from(IncidentVerification v) {
        if (v == null) return null;
        return new IncidentVerificationResponse(
                v.getId(),
                v.getIncidentId(),
                v.getActionId(),
                v.getVerifierId(),
                v.getVerifierName(),
                v.getVerifiedAt(),
                v.getVerificationMethod(),
                v.getResult(),
                v.getExplanatoryNotes(),
                v.getSupportingEvidenceUri()
        );
    }
}
