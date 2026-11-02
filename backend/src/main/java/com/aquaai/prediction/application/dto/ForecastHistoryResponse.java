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
public class ForecastHistoryResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryItem {
        private UUID id;
        private UUID fieldId;
        private String fieldName;
        private String predictionType;
        private String horizon;
        private Instant generatedAt;
        private Instant targetTime;
        private BigDecimal predictedValue;
        private BigDecimal actualOutcomeValue;
        private String unit;
        private int confidencePercent;
        private String accuracyStatus;
        private String evidence;
    }

    private int totalEvaluated;
    private int verifiedAccurateCount;
    private int verifiedInaccurateCount;
    private int pendingCount;
    private BigDecimal accuracyPercent;
    private List<HistoryItem> history;
}
