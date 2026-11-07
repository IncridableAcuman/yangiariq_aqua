package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Evidence-based water conservation opportunity identified through regional accounting audits.
 */
@Getter
@Builder(toBuilder = true)
public class ConservationOpportunity {

    private final UUID id;
    private final String title;
    private final String problemDescription;
    private final String category; // CONVEYANCE_LOSS, OVER_DELIVERY, IRRIGATION_INEFFICIENCY, METER_CALIBRATION
    private final String affectedComponentType; // CANAL, FIELD, WUA, INTAKE
    private final UUID affectedComponentId;
    private final String affectedComponentName;
    private final BigDecimal estimatedPotentialSavingsM3;
    private final String estimationMethodology;
    private final String confidence;
    private final String operationalImpact;
    private final String suggestedAction;
    private final String targetWorkflowUri;
    private final EvidenceClassification evidenceClassification;
    private final List<String> supportingEvidence;

    public ConservationOpportunity(
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
            EvidenceClassification evidenceClassification,
            List<String> supportingEvidence
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = title != null ? title : "Conservation Opportunity";
        this.problemDescription = problemDescription != null ? problemDescription : "";
        this.category = category != null ? category : "CONVEYANCE_LOSS";
        this.affectedComponentType = affectedComponentType != null ? affectedComponentType : "CANAL";
        this.affectedComponentId = affectedComponentId;
        this.affectedComponentName = affectedComponentName != null ? affectedComponentName : "Component";
        this.estimatedPotentialSavingsM3 = estimatedPotentialSavingsM3; // Can be null if not calculable
        this.estimationMethodology = estimationMethodology != null ? estimationMethodology : "Empirical physical variance";
        this.confidence = confidence != null ? confidence : "HIGH";
        this.operationalImpact = operationalImpact != null ? operationalImpact : "LOW";
        this.suggestedAction = suggestedAction != null ? suggestedAction : "";
        this.targetWorkflowUri = targetWorkflowUri != null ? targetWorkflowUri : "/network";
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.supportingEvidence = supportingEvidence != null ? supportingEvidence : List.of();
    }
}
