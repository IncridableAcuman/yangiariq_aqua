package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.IrrigationNeedRankingResponse;
import com.aquaai.prediction.application.dto.IrrigationNeedRankingResponse.RankedFieldItem;
import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.IrrigationLikelihood;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionProvider;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetIrrigationNeedRankingUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionProvider predictionProvider;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public IrrigationNeedRankingResponse execute(String horizonStr) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(horizonStr, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public IrrigationNeedRankingResponse execute(
            String horizonStr,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        PredictionHorizon horizon = PredictionHorizon.fromString(horizonStr);

        List<RankedFieldItem> items = new ArrayList<>();
        int urgentCount = 0;

        for (FieldMetadata field : fields) {
            Prediction moisturePred = predictionProvider.generateSpecificPrediction(field, PredictionType.SOIL_MOISTURE_FORECAST, horizon, dataProvider);
            Prediction needPred = predictionProvider.generateSpecificPrediction(field, PredictionType.IRRIGATION_NEED_FORECAST, horizon, dataProvider);

            IrrigationLikelihood likelihood = needPred.getLikelihood() != null ? needPred.getLikelihood() : IrrigationLikelihood.UNKNOWN;
            if (likelihood == IrrigationLikelihood.HIGHLY_LIKELY || likelihood == IrrigationLikelihood.LIKELY) {
                urgentCount++;
            }

            BigDecimal currentMoist = null;
            if (moisturePred.getDataSufficiency() != DataSufficiency.INSUFFICIENT) {
                currentMoist = moisturePred.getPredictedValue();
            }

            items.add(RankedFieldItem.builder()
                    .fieldId(field.fieldId())
                    .fieldName(field.name())
                    .cropType(field.cropType())
                    .areaHectares(field.areaHectares())
                    .likelihood(likelihood.name())
                    .riskLevel(needPred.getRiskLevel().name())
                    .currentMoisturePercent(currentMoist)
                    .forecastMoisturePercent(moisturePred.getPredictedValue())
                    .expectedVolumeM3(needPred.getPredictedValue() != null ? needPred.getPredictedValue().setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                    .confidencePercent(needPred.getConfidencePercent())
                    .dataSufficiency(needPred.getDataSufficiency().name())
                    .recommendedAction(needPred.getRecommendedAction())
                    .evidence(needPred.getEvidence())
                    .build());
        }

        // Sort by urgency priority
        items.sort(Comparator.comparingInt(this::urgencyWeight));

        return IrrigationNeedRankingResponse.builder()
                .horizon(horizon.name())
                .totalFieldsCount(fields.size())
                .urgentCount(urgentCount)
                .rankedFields(items)
                .generatedAt(Instant.now())
                .build();
    }

    private int urgencyWeight(RankedFieldItem item) {
        return switch (item.getLikelihood()) {
            case "HIGHLY_LIKELY" -> 1;
            case "LIKELY" -> 2;
            case "POSSIBLE" -> 3;
            case "LOW_LIKELIHOOD" -> 4;
            case "NOT_EXPECTED" -> 5;
            default -> 6; // UNKNOWN
        };
    }
}
