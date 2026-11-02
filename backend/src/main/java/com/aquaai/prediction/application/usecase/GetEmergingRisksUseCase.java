package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionItemDto;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionProvider;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEmergingRisksUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionProvider predictionProvider;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<PredictionItemDto> execute(String horizonStr) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(horizonStr, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public List<PredictionItemDto> execute(
            String horizonStr,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        PredictionHorizon horizon = PredictionHorizon.fromString(horizonStr);

        List<PredictionItemDto> risks = new ArrayList<>();

        for (FieldMetadata field : fields) {
            List<Prediction> fieldPreds = predictionProvider.generateFieldPredictions(field, horizon, dataProvider);
            for (Prediction p : fieldPreds) {
                if (p.getRiskLevel() == RiskLevel.BLOCKED ||
                    p.getRiskLevel() == RiskLevel.CRITICAL ||
                    p.getRiskLevel() == RiskLevel.HIGH) {
                    risks.add(PredictionItemDto.fromDomain(p, field.name()));
                }
            }
        }

        risks.sort(Comparator.comparingInt(this::riskPriority));
        return risks;
    }

    private int riskPriority(PredictionItemDto item) {
        if ("BLOCKED".equalsIgnoreCase(item.getRiskLevel())) return 1;
        if ("CRITICAL".equalsIgnoreCase(item.getRiskLevel())) return 2;
        if ("HIGH".equalsIgnoreCase(item.getRiskLevel())) return 3;
        return 4;
    }
}
