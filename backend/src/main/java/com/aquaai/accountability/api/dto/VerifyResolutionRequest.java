package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.VerificationOutcome;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VerifyResolutionRequest(
        UUID actionId,
        @NotBlank @Size(max = 128) String verificationMethod,
        @NotNull VerificationOutcome result,
        @NotBlank @Size(max = 2000) String explanatoryNotes,
        @Size(max = 1000) String supportingEvidenceUri,
        Boolean administrativeOverride
) {}
