package com.aquaai.risk.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An explainable risk contributor evaluated within a specific domain category.
 * Explains its contribution score, consequences if unaddressed, and missing inputs.
 */
public record RiskFactor(
        UUID id,
        RiskCategory category,
        RiskSeverity severity,
        RiskConfidence confidence,
        String title,
        String description,
        List<RiskEvidence> evidenceList,
        BigDecimal contributionScore,
        String potentialImpact,
        String suggestedAction,
        List<String> missingInputs
) {
    public RiskFactor {
        id = id != null ? id : UUID.randomUUID();
        category = Objects.requireNonNull(category, "RiskCategory must not be null");
        severity = Objects.requireNonNull(severity, "RiskSeverity must not be null");
        confidence = Objects.requireNonNull(confidence, "RiskConfidence must not be null");
        title = Objects.requireNonNull(title, "title must not be null");
        description = Objects.requireNonNull(description, "description must not be null");
        evidenceList = evidenceList != null ? Collections.unmodifiableList(evidenceList) : Collections.emptyList();
        contributionScore = contributionScore != null ? contributionScore : BigDecimal.ZERO;
        potentialImpact = potentialImpact != null ? potentialImpact : "";
        suggestedAction = suggestedAction != null ? suggestedAction : "";
        missingInputs = missingInputs != null ? Collections.unmodifiableList(missingInputs) : Collections.emptyList();
    }
}
