package com.aquaai.prediction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionOverviewResponse {

    private String horizon;
    private int totalParcelsCount;
    private int fieldsNeedingIrrigationCount;
    private BigDecimal totalWaterDemandM3;
    private BigDecimal waterDemandLowerBoundM3;
    private BigDecimal waterDemandUpperBoundM3;
    private int fieldsAtWaterRiskCount;
    private int sensorHealthPercent;
    private int activeHydraulicBlocksCount;
    private List<PredictionItemDto> urgentForecasts;
    private Instant generatedAt;
}
