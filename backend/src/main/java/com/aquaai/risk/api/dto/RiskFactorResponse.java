package com.aquaai.risk.api.dto;

import com.aquaai.risk.domain.model.RiskCategory;
import com.aquaai.risk.domain.model.RiskConfidence;
import com.aquaai.risk.domain.model.RiskFactor;
import com.aquaai.risk.domain.model.RiskSeverity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record RiskFactorResponse(
        UUID id,
        RiskCategory category,
        RiskSeverity severity,
        RiskConfidence confidence,
        String title,
        String description,
        List<RiskEvidenceResponse> evidenceList,
        BigDecimal contributionScore,
        String potentialImpact,
        String suggestedAction,
        List<String> missingInputs
) {
    public static RiskFactorResponse fromDomain(RiskFactor f) {
        if (f == null) return null;
        List<RiskEvidenceResponse> evList = f.evidenceList() != null
                ? f.evidenceList().stream().map(RiskEvidenceResponse::fromDomain).toList()
                : Collections.emptyList();

        return new RiskFactorResponse(
                f.id(),
                f.category(),
                f.severity(),
                f.confidence(),
                f.title(),
                f.description(),
                evList,
                f.contributionScore(),
                f.potentialImpact(),
                f.suggestedAction(),
                f.missingInputs()
        );
    }
}
