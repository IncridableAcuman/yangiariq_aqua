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
public class IrrigationNeedRankingResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankedFieldItem {
        private UUID fieldId;
        private String fieldName;
        private String cropType;
        private BigDecimal areaHectares;
        private String likelihood;
        private String riskLevel;
        private BigDecimal currentMoisturePercent;
        private BigDecimal forecastMoisturePercent;
        private BigDecimal expectedVolumeM3;
        private int confidencePercent;
        private String dataSufficiency;
        private String recommendedAction;
        private String evidence;
    }

    private String horizon;
    private int totalFieldsCount;
    private int urgentCount;
    private List<RankedFieldItem> rankedFields;
    private Instant generatedAt;
}
