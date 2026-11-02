package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionItemDto;
import com.aquaai.prediction.application.dto.PredictionOverviewResponse;
import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionProvider;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFarmPredictionOverviewUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionProvider predictionProvider;
    private final PredictionRepository predictionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public PredictionOverviewResponse execute(String horizonStr) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(horizonStr, currentUserId, currentRole);
    }

    @Transactional
    public PredictionOverviewResponse execute(
            String horizonStr,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);

        PredictionHorizon horizon = PredictionHorizon.fromString(horizonStr);

        int fieldsNeedingIrrigationCount = 0;
        BigDecimal totalDemand = BigDecimal.ZERO;
        BigDecimal totalLower = BigDecimal.ZERO;
        BigDecimal totalUpper = BigDecimal.ZERO;
        int fieldsAtWaterRiskCount = 0;
        int activeHydraulicBlocksCount = 0;
        long totalSensorScoreSum = 0;
        int sensorScoreCount = 0;

        List<Prediction> allGeneratedPredictions = new ArrayList<>();
        List<PredictionItemDto> urgentForecasts = new ArrayList<>();

        for (FieldMetadata field : fields) {
            List<Prediction> fieldPreds = predictionProvider.generateFieldPredictions(field, horizon, dataProvider);
            allGeneratedPredictions.addAll(fieldPreds);

            for (Prediction p : fieldPreds) {
                // Check urgent conditions
                if (p.getRiskLevel() == RiskLevel.CRITICAL ||
                    p.getRiskLevel() == RiskLevel.BLOCKED ||
                    p.getRiskLevel() == RiskLevel.HIGH) {
                    urgentForecasts.add(PredictionItemDto.fromDomain(p, field.name()));
                }

                if (p.getPredictionType() == PredictionType.IRRIGATION_NEED_FORECAST) {
                    if (p.getLikelihood() == IrrigationLikelihood.HIGHLY_LIKELY ||
                        p.getLikelihood() == IrrigationLikelihood.LIKELY) {
                        fieldsNeedingIrrigationCount++;
                    }
                } else if (p.getPredictionType() == PredictionType.WATER_DEMAND_FORECAST) {
                    if (p.getPredictedValue() != null) {
                        totalDemand = totalDemand.add(p.getPredictedValue());
                    }
                    if (p.getLowerBound() != null) {
                        totalLower = totalLower.add(p.getLowerBound());
                    }
                    if (p.getUpperBound() != null) {
                        totalUpper = totalUpper.add(p.getUpperBound());
                    }
                } else if (p.getPredictionType() == PredictionType.WATER_AVAILABILITY_RISK) {
                    if (p.getRiskLevel() == RiskLevel.HIGH || p.getRiskLevel() == RiskLevel.CRITICAL) {
                        fieldsAtWaterRiskCount++;
                    }
                } else if (p.getPredictionType() == PredictionType.IRRIGATION_PLAN_RISK) {
                    if (p.getRiskLevel() == RiskLevel.BLOCKED) {
                        activeHydraulicBlocksCount++;
                    }
                } else if (p.getPredictionType() == PredictionType.SENSOR_RELIABILITY_FORECAST) {
                    if (p.getPredictedValue() != null) {
                        totalSensorScoreSum += p.getPredictedValue().longValue();
                        sensorScoreCount++;
                    }
                }
            }
        }

        // Persist batch
        try {
            predictionRepository.saveAll(allGeneratedPredictions);
        } catch (Exception e) {
            log.warn("Failed to persist overview predictions batch: {}", e.getMessage());
        }

        int sensorHealthPercent = sensorScoreCount > 0
                ? (int) Math.round((double) totalSensorScoreSum / sensorScoreCount)
                : 95;

        // Sort urgent forecasts by risk priority (BLOCKED -> CRITICAL -> HIGH)
        urgentForecasts.sort(Comparator.comparingInt(this::riskPriority));

        // Cap urgent forecasts to top 10
        List<PredictionItemDto> topUrgent = urgentForecasts.stream().limit(10).toList();

        return PredictionOverviewResponse.builder()
                .horizon(horizon.name())
                .totalParcelsCount(fields.size())
                .fieldsNeedingIrrigationCount(fieldsNeedingIrrigationCount)
                .totalWaterDemandM3(totalDemand.setScale(0, RoundingMode.HALF_UP))
                .waterDemandLowerBoundM3(totalLower.setScale(0, RoundingMode.HALF_UP))
                .waterDemandUpperBoundM3(totalUpper.setScale(0, RoundingMode.HALF_UP))
                .fieldsAtWaterRiskCount(fieldsAtWaterRiskCount)
                .sensorHealthPercent(sensorHealthPercent)
                .activeHydraulicBlocksCount(activeHydraulicBlocksCount)
                .urgentForecasts(topUrgent)
                .generatedAt(Instant.now())
                .build();
    }

    private int riskPriority(PredictionItemDto item) {
        if ("BLOCKED".equalsIgnoreCase(item.getRiskLevel())) return 1;
        if ("CRITICAL".equalsIgnoreCase(item.getRiskLevel())) return 2;
        if ("HIGH".equalsIgnoreCase(item.getRiskLevel())) return 3;
        if ("MEDIUM".equalsIgnoreCase(item.getRiskLevel())) return 4;
        return 5;
    }
}
