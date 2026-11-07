package com.aquaai.wateraccounting.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Audit record of conveyance loss across a specific canal segment.
 */
@Getter
@Builder(toBuilder = true)
public class ConveyanceLossAssessment {

    private final UUID edgeId;
    private final String canalName;
    private final String edgeType;
    private final String startNodeName;
    private final String endNodeName;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final BigDecimal upstreamVolumeM3;
    private final BigDecimal downstreamVolumeM3;
    private final BigDecimal lossVolumeM3;
    private final BigDecimal lossPercentage;
    private final ConveyanceLossClassification classification;
    private final EvidenceClassification evidenceClassification;
    private final String confidence;
    private final String explanation;

    public ConveyanceLossAssessment(
            UUID edgeId,
            String canalName,
            String edgeType,
            String startNodeName,
            String endNodeName,
            Instant periodStart,
            Instant periodEnd,
            BigDecimal upstreamVolumeM3,
            BigDecimal downstreamVolumeM3,
            BigDecimal lossVolumeM3,
            BigDecimal lossPercentage,
            ConveyanceLossClassification classification,
            EvidenceClassification evidenceClassification,
            String confidence,
            String explanation
    ) {
        this.edgeId = edgeId;
        this.canalName = canalName != null ? canalName : "Canal";
        this.edgeType = edgeType != null ? edgeType : "PRIMARY_CANAL";
        this.startNodeName = startNodeName != null ? startNodeName : "Start Node";
        this.endNodeName = endNodeName != null ? endNodeName : "End Node";
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.upstreamVolumeM3 = upstreamVolumeM3 != null ? upstreamVolumeM3 : BigDecimal.ZERO;
        this.downstreamVolumeM3 = downstreamVolumeM3 != null ? downstreamVolumeM3 : BigDecimal.ZERO;
        this.lossVolumeM3 = lossVolumeM3 != null ? lossVolumeM3 : BigDecimal.ZERO;
        this.lossPercentage = lossPercentage != null ? lossPercentage : BigDecimal.ZERO;
        this.classification = classification != null ? classification : ConveyanceLossClassification.NO_SIGNIFICANT_DISCREPANCY;
        this.evidenceClassification = evidenceClassification != null ? evidenceClassification : EvidenceClassification.CALCULATED;
        this.confidence = confidence != null ? confidence : "HIGH";
        this.explanation = explanation;
    }
}
