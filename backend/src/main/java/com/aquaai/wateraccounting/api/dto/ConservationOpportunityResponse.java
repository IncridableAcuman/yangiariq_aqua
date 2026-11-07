package com.aquaai.wateraccounting.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ConservationOpportunityResponse(
        UUID id,
        String title,
        String problemDescription,
        String category,
        String affectedComponentType,
        UUID affectedComponentId,
        String affectedComponentName,
        BigDecimal estimatedPotentialSavingsM3,
        String estimationMethodology,
        String confidence,
        String operationalImpact,
        String suggestedAction,
        String targetWorkflowUri,
        String evidenceClassification,
        List<String> supportingEvidence
) {}
