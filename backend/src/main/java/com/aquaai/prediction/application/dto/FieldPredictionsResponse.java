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
public class FieldPredictionsResponse {

    private UUID fieldId;
    private String fieldName;
    private String cropType;
    private String soilType;
    private String irrigationMethod;
    private BigDecimal areaHectares;
    private String horizon;
    private PredictionItemDto soilMoisture;
    private PredictionItemDto irrigationNeed;
    private PredictionItemDto waterDemand;
    private PredictionItemDto waterAvailability;
    private PredictionItemDto irrigationPlanRisk;
    private PredictionItemDto sensorReliability;
    private List<PredictionItemDto> allPredictions;
    private Instant generatedAt;
}
