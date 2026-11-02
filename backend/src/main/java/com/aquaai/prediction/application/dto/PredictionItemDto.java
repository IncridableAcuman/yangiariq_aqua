package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.Prediction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionItemDto {

    private UUID id;
    private UUID fieldId;
    private String fieldName;
    private String predictionType;
    private String horizon;
    private Instant targetTime;
    private BigDecimal predictedValue;
    private BigDecimal lowerBound;
    private BigDecimal upperBound;
    private String unit;
    private int confidencePercent;
    private String riskLevel;
    private String dataSufficiency;
    private String likelihood;
    private String evidence;
    private String assumptions;
    private String recommendedAction;
    private String actionTargetModule;
    private BigDecimal actualOutcomeValue;
    private String accuracyStatus;
    private Instant generatedAt;

    public static PredictionItemDto fromDomain(Prediction p, String fieldName) {
        if (p == null) return null;
        return PredictionItemDto.builder()
                .id(p.getId())
                .fieldId(p.getFieldId())
                .fieldName(fieldName)
                .predictionType(p.getPredictionType().name())
                .horizon(p.getHorizon().name())
                .targetTime(p.getTargetTime())
                .predictedValue(p.getPredictedValue())
                .lowerBound(p.getLowerBound())
                .upperBound(p.getUpperBound())
                .unit(p.getUnit())
                .confidencePercent(p.getConfidencePercent())
                .riskLevel(p.getRiskLevel().name())
                .dataSufficiency(p.getDataSufficiency().name())
                .likelihood(p.getLikelihood() != null ? p.getLikelihood().name() : null)
                .evidence(p.getEvidence())
                .assumptions(p.getAssumptions())
                .recommendedAction(p.getRecommendedAction())
                .actionTargetModule(p.getActionTargetModule())
                .actualOutcomeValue(p.getActualOutcomeValue())
                .accuracyStatus(p.getAccuracyStatus().name())
                .generatedAt(p.getGeneratedAt())
                .build();
    }
}
