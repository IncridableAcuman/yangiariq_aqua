package com.aquaai.accountability.api.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;
import jakarta.validation.constraints.Size;

public record RecordInvestigationRequest(
        @Size(max = 128) String rootCauseCategory,
        @Size(max = 2000) String rootCauseDetails,
        EvidenceConfidence updatedConfidence,
        @Size(max = 2000) String notes
) {}
