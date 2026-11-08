package com.aquaai.accountability.application.dto;

import com.aquaai.accountability.domain.model.EvidenceConfidence;

import java.util.UUID;

public record RecordInvestigationCommand(
        UUID incidentId,
        String rootCauseCategory,
        String rootCauseDetails,
        EvidenceConfidence updatedConfidence,
        String notes,
        UUID investigatorId,
        String investigatorName,
        String investigatorRole
) {}
