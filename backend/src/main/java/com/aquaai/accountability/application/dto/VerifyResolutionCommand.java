package com.aquaai.accountability.application.dto;

import com.aquaai.accountability.domain.model.VerificationOutcome;

import java.util.UUID;

public record VerifyResolutionCommand(
        UUID incidentId,
        UUID actionId,
        String verificationMethod,
        VerificationOutcome result,
        String explanatoryNotes,
        String supportingEvidenceUri,
        boolean administrativeOverride,
        UUID verifierId,
        String verifierName,
        String verifierRole
) {}
