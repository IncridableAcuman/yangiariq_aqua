package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.FieldPredictionsResponse;
import com.aquaai.prediction.application.dto.PredictionItemDto;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionProvider;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFieldPredictionsUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionProvider predictionProvider;
    private final PredictionRepository predictionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public FieldPredictionsResponse execute(UUID fieldId, String horizonStr) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(fieldId, horizonStr, currentUserId, currentRole);
    }

    @Transactional
    public FieldPredictionsResponse execute(
            UUID fieldId,
            String horizonStr,
            UUID currentUserId,
            UserRole currentRole
    ) {
        if (fieldId == null) {
            throw new ResourceNotFoundException("Field", "null");
        }

        FieldMetadata field = dataProvider.getFieldMetadata(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        // Anti-IDOR check: FARMER can only access own field
        if (currentRole == UserRole.FARMER) {
            if (field.ownerId() == null || !field.ownerId().equals(currentUserId)) {
                // Return 404 to prevent resource existence enumeration
                throw new ResourceNotFoundException("Field", fieldId);
            }
        }

        PredictionHorizon horizon = PredictionHorizon.fromString(horizonStr);

        // Generate forecasts using model provider
        List<Prediction> predictions = predictionProvider.generateFieldPredictions(field, horizon, dataProvider);

        // Persist to database for auditing and forecast history
        try {
            predictionRepository.saveAll(predictions);
        } catch (Exception e) {
            log.warn("Failed to persist predictions for field {}: {}", fieldId, e.getMessage());
        }

        PredictionItemDto soilMoisture = null;
        PredictionItemDto irrigationNeed = null;
        PredictionItemDto waterDemand = null;
        PredictionItemDto waterAvailability = null;
        PredictionItemDto irrigationPlanRisk = null;
        PredictionItemDto sensorReliability = null;

        for (Prediction p : predictions) {
            PredictionItemDto dto = PredictionItemDto.fromDomain(p, field.name());
            switch (p.getPredictionType()) {
                case SOIL_MOISTURE_FORECAST -> soilMoisture = dto;
                case IRRIGATION_NEED_FORECAST -> irrigationNeed = dto;
                case WATER_DEMAND_FORECAST -> waterDemand = dto;
                case WATER_AVAILABILITY_RISK -> waterAvailability = dto;
                case IRRIGATION_PLAN_RISK -> irrigationPlanRisk = dto;
                case SENSOR_RELIABILITY_FORECAST -> sensorReliability = dto;
            }
        }

        List<PredictionItemDto> allItems = predictions.stream()
                .map(p -> PredictionItemDto.fromDomain(p, field.name()))
                .toList();

        return FieldPredictionsResponse.builder()
                .fieldId(field.fieldId())
                .fieldName(field.name())
                .cropType(field.cropType())
                .soilType(field.soilType())
                .irrigationMethod(field.irrigationMethod())
                .areaHectares(field.areaHectares())
                .horizon(horizon.name())
                .soilMoisture(soilMoisture)
                .irrigationNeed(irrigationNeed)
                .waterDemand(waterDemand)
                .waterAvailability(waterAvailability)
                .irrigationPlanRisk(irrigationPlanRisk)
                .sensorReliability(sensorReliability)
                .allPredictions(allItems)
                .generatedAt(Instant.now())
                .build();
    }
}
