package com.aquaai.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterDemandForecastResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldDemandItem {
        private UUID fieldId;
        private String fieldName;
        private String cropType;
        private BigDecimal areaHectares;
        private BigDecimal expectedDemandM3;
        private BigDecimal lowerBoundM3;
        private BigDecimal upperBoundM3;
        private int confidencePercent;
        private String riskLevel;
        private String assumptions;
    }

    private String horizon;
    private BigDecimal totalExpectedDemandM3;
    private BigDecimal totalLowerBoundM3;
    private BigDecimal totalUpperBoundM3;
    private List<FieldDemandItem> fieldDemands;
    private Instant generatedAt;
}
