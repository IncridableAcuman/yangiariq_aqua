package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.FieldEvaluationDto;
import com.aquaai.prediction.application.dto.FieldEvaluationResponse;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.service.PredictionEvaluationAggregator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPredictionEvaluationByFieldUseCase {

    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;
    private final PredictionEvaluationAggregator aggregator = new PredictionEvaluationAggregator();

    @Transactional(readOnly = true)
    public FieldEvaluationResponse execute(Instant from, Instant to, int page, int size) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(from, to, page, size, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public FieldEvaluationResponse execute(
            Instant from,
            Instant to,
            int page,
            int size,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> accessibleFields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);

        Instant safeTo = to != null ? to : Instant.now();
        Instant safeFrom = from != null ? from : safeTo.minus(Duration.ofDays(30));

        List<FieldEvaluationDto> list = new ArrayList<>();

        for (FieldMetadata field : accessibleFields) {
            List<PredictionEvaluation> evals = evaluationRepository.findEvaluationsByField(field.fieldId(), safeFrom, safeTo);
            int total = evals.size();
            List<PredictionEvaluation> evaluated = evals.stream().filter(e -> e.getEvaluationStatus() == EvaluationStatus.EVALUATED).toList();
            int evaluatedCount = evaluated.size();
            boolean sufficient = evaluatedCount >= PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE;

            BigDecimal mae = null;
            BigDecimal acc = null;

            if (sufficient) {
                var num = aggregator.calculateNumericalMetrics(evaluated, PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE);
                if (num.isSufficientSample()) {
                    mae = num.meanAbsoluteError();
                }
                var cl = aggregator.calculateClassificationMetrics(evaluated, PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE);
                if (cl.isSufficientSample()) {
                    acc = cl.accuracyPercent();
                }
            }

            Map<String, Integer> qualityBreakdown = new HashMap<>();
            qualityBreakdown.put(EvaluationDataQuality.DATA_COMPLETE.name(), 0);
            qualityBreakdown.put(EvaluationDataQuality.DATA_PARTIAL.name(), 0);
            qualityBreakdown.put(EvaluationDataQuality.DATA_INSUFFICIENT.name(), 0);

            for (PredictionEvaluation e : evals) {
                String qName = e.getDataQuality() != null ? e.getDataQuality().name() : EvaluationDataQuality.DATA_COMPLETE.name();
                qualityBreakdown.put(qName, qualityBreakdown.getOrDefault(qName, 0) + 1);
            }

            ModelHealthStatus healthStatus = sufficient
                    ? (mae != null && mae.doubleValue() > 10.0 ? ModelHealthStatus.WATCH : ModelHealthStatus.HEALTHY)
                    : ModelHealthStatus.INSUFFICIENT_DATA;

            EvaluationStatus status = evaluatedCount > 0 ? EvaluationStatus.EVALUATED : (total > 0 ? EvaluationStatus.PENDING : EvaluationStatus.INSUFFICIENT_DATA);

            list.add(FieldEvaluationDto.builder()
                    .fieldId(field.fieldId())
                    .fieldName(field.name())
                    .cropType(field.cropType())
                    .areaHectares(field.areaHectares())
                    .totalPredictions(total)
                    .evaluatedCount(evaluatedCount)
                    .isSufficientSample(sufficient)
                    .meanAbsoluteError(mae)
                    .accuracyPercent(acc)
                    .evaluationStatus(status)
                    .healthStatus(healthStatus)
                    .dataQualityBreakdown(qualityBreakdown)
                    .build());
        }

        int totalElements = list.size();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        int fromIdx = Math.min(safePage * safeSize, totalElements);
        int toIdx = Math.min(fromIdx + safeSize, totalElements);
        List<FieldEvaluationDto> pagedContent = list.subList(fromIdx, toIdx);
        int totalPages = (int) Math.ceil((double) totalElements / safeSize);

        return FieldEvaluationResponse.builder()
                .fields(pagedContent)
                .page(safePage)
                .size(safeSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
