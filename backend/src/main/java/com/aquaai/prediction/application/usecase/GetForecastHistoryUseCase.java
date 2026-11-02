package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.ForecastHistoryResponse;
import com.aquaai.prediction.application.dto.ForecastHistoryResponse.HistoryItem;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionAccuracyStatus;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetForecastHistoryUseCase {

    private final PredictionDataProvider dataProvider;
    private final PredictionRepository predictionRepository;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public ForecastHistoryResponse execute(Instant from, Instant to, int limit) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(from, to, limit, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public ForecastHistoryResponse execute(
            Instant from,
            Instant to,
            int limit,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        List<UUID> fieldIds = fields.stream().map(FieldMetadata::fieldId).toList();
        Map<UUID, String> fieldNameMap = fields.stream().collect(Collectors.toMap(FieldMetadata::fieldId, FieldMetadata::name, (a, b) -> a));

        Instant safeTo = to != null ? to : Instant.now();
        Instant safeFrom = from != null ? from : safeTo.minus(Duration.ofDays(30));

        List<Prediction> historyPredictions = predictionRepository.findForecastHistory(fieldIds, safeFrom, safeTo, limit);

        int accurate = 0;
        int inaccurate = 0;
        int pending = 0;
        List<HistoryItem> items = new ArrayList<>();

        for (Prediction p : historyPredictions) {
            if (p.getAccuracyStatus() == PredictionAccuracyStatus.VERIFIED_ACCURATE) {
                accurate++;
            } else if (p.getAccuracyStatus() == PredictionAccuracyStatus.VERIFIED_INACCURATE) {
                inaccurate++;
            } else if (p.getAccuracyStatus() == PredictionAccuracyStatus.PENDING) {
                pending++;
            }

            items.add(HistoryItem.builder()
                    .id(p.getId())
                    .fieldId(p.getFieldId())
                    .fieldName(fieldNameMap.getOrDefault(p.getFieldId(), "Field"))
                    .predictionType(p.getPredictionType().name())
                    .horizon(p.getHorizon().name())
                    .generatedAt(p.getGeneratedAt())
                    .targetTime(p.getTargetTime())
                    .predictedValue(p.getPredictedValue())
                    .actualOutcomeValue(p.getActualOutcomeValue())
                    .unit(p.getUnit())
                    .confidencePercent(p.getConfidencePercent())
                    .accuracyStatus(p.getAccuracyStatus().name())
                    .evidence(p.getEvidence())
                    .build());
        }

        int totalEvaluated = accurate + inaccurate;
        BigDecimal accuracyPercent = totalEvaluated > 0
                ? BigDecimal.valueOf((double) accurate / totalEvaluated * 100.0).setScale(1, RoundingMode.HALF_UP)
                : null; // null represents "Unavailable: insufficient completed forecasts"

        return ForecastHistoryResponse.builder()
                .totalEvaluated(totalEvaluated)
                .verifiedAccurateCount(accurate)
                .verifiedInaccurateCount(inaccurate)
                .pendingCount(pending)
                .accuracyPercent(accuracyPercent)
                .history(items)
                .build();
    }
}
