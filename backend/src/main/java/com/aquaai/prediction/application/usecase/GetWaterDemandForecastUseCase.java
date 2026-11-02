package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.WaterDemandForecastResponse;
import com.aquaai.prediction.application.dto.WaterDemandForecastResponse.FieldDemandItem;
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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetWaterDemandForecastUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionProvider predictionProvider;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public WaterDemandForecastResponse execute(String horizonStr) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(horizonStr, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public WaterDemandForecastResponse execute(
            String horizonStr,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        PredictionHorizon horizon = PredictionHorizon.fromString(horizonStr);

        BigDecimal totalDemand = BigDecimal.ZERO;
        BigDecimal totalLower = BigDecimal.ZERO;
        BigDecimal totalUpper = BigDecimal.ZERO;

        List<FieldDemandItem> items = new ArrayList<>();

        for (FieldMetadata field : fields) {
            Prediction p = predictionProvider.generateSpecificPrediction(field, PredictionType.WATER_DEMAND_FORECAST, horizon, dataProvider);

            BigDecimal demand = p.getPredictedValue() != null ? p.getPredictedValue() : BigDecimal.ZERO;
            BigDecimal lower = p.getLowerBound() != null ? p.getLowerBound() : BigDecimal.ZERO;
            BigDecimal upper = p.getUpperBound() != null ? p.getUpperBound() : BigDecimal.ZERO;

            totalDemand = totalDemand.add(demand);
            totalLower = totalLower.add(lower);
            totalUpper = totalUpper.add(upper);

            items.add(FieldDemandItem.builder()
                    .fieldId(field.fieldId())
                    .fieldName(field.name())
                    .cropType(field.cropType())
                    .areaHectares(field.areaHectares())
                    .expectedDemandM3(demand.setScale(0, RoundingMode.HALF_UP))
                    .lowerBoundM3(lower.setScale(0, RoundingMode.HALF_UP))
                    .upperBoundM3(upper.setScale(0, RoundingMode.HALF_UP))
                    .confidencePercent(p.getConfidencePercent())
                    .riskLevel(p.getRiskLevel().name())
                    .assumptions(p.getAssumptions())
                    .build());
        }

        return WaterDemandForecastResponse.builder()
                .horizon(horizon.name())
                .totalExpectedDemandM3(totalDemand.setScale(0, RoundingMode.HALF_UP))
                .totalLowerBoundM3(totalLower.setScale(0, RoundingMode.HALF_UP))
                .totalUpperBoundM3(totalUpper.setScale(0, RoundingMode.HALF_UP))
                .fieldDemands(items)
                .generatedAt(Instant.now())
                .build();
    }
}
