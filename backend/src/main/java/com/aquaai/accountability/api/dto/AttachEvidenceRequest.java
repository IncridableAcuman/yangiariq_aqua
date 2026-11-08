package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttachEvidenceRequest(
        @NotBlank @Size(max = 64) String evidenceType,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        @Size(max = 1000) String evidenceUri,
        @NotNull EvidenceConfidence confidence,
        Boolean isSimulated,
        @Size(max = 255) String sourceReference
) {}
