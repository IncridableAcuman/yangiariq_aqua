package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionEvaluationDetailDto;
import com.aquaai.prediction.application.dto.PredictionTypePerformanceDto;
import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.service.PredictionEvaluationAggregator;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPredictionEvaluationByTypeUseCase {

    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;
    private final PredictionEvaluationAggregator aggregator = new PredictionEvaluationAggregator();

    @Transactional(readOnly = true)
    public List<PredictionTypePerformanceDto> execute(PredictionType specificType, Instant from, Instant to) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(specificType, from, to, currentUserId, currentRole);
    }

    @Transactional(readOnly = true)
    public List<PredictionTypePerformanceDto> execute(
            PredictionType specificType,
            Instant from,
            Instant to,
            UUID currentUserId,
            UserRole currentRole
    ) {
        boolean isAdminOrOperator = currentRole == UserRole.ADMIN || currentRole == UserRole.OPERATOR;
        List<FieldMetadata> fields = dataProvider.getAccessibleFields(currentUserId, isAdminOrOperator);
        List<UUID> fieldIds = fields.stream().map(FieldMetadata::fieldId).toList();
        Map<UUID, String> fieldNameMap = fields.stream().collect(Collectors.toMap(FieldMetadata::fieldId, FieldMetadata::name, (a, b) -> a));

        Instant safeTo = to != null ? to : Instant.now();
        Instant safeFrom = from != null ? from : safeTo.minus(Duration.ofDays(30));

        List<PredictionEvaluation> evaluations = evaluationRepository.findEvaluationsForPeriod(fieldIds, safeFrom, safeTo);

        List<PredictionTypePerformanceDto> result = new ArrayList<>();
        PredictionType[] typesToProcess = specificType != null ? new PredictionType[]{specificType} : PredictionType.values();

        for (PredictionType type : typesToProcess) {
            List<PredictionEvaluation> forType = evaluations.stream()
                    .filter(e -> e.getPredictionType() == type)
                    .toList();

            boolean isNumerical = type == PredictionType.SOIL_MOISTURE_FORECAST || type == PredictionType.WATER_DEMAND_FORECAST;
            String category = isNumerical ? "NUMERICAL" : "CLASSIFICATION";

            NumericalEvaluationMetrics numMetrics = isNumerical ? aggregator.calculateNumericalMetrics(forType) : null;
            ClassificationEvaluationMetrics classMetrics = !isNumerical ? aggregator.calculateClassificationMetrics(forType) : null;

            int sample = forType.size();
            boolean sufficient = sample >= PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE;

            var health = aggregator.determineModelHealth(numMetrics, classMetrics, sample, PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE);

            List<PredictionEvaluationDetailDto> largestDeviations = forType.stream()
                    .filter(e -> e.getAbsoluteError() != null)
                    .sorted(Comparator.comparing(PredictionEvaluation::getAbsoluteError, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .map(e -> PredictionEvaluationDetailDto.fromDomain(e, fieldNameMap.get(e.getFieldId())))
                    .toList();

            String displayName = switch (type) {
                case SOIL_MOISTURE_FORECAST -> "Tuproq Namligi Prognozi";
                case IRRIGATION_NEED_FORECAST -> "Sug‘orish Ehtiyoji Bashorati";
                case WATER_DEMAND_FORECAST -> "Suv Talabi Prognozi";
                case SENSOR_RELIABILITY_FORECAST -> "Datchik Ishonchliligi Bashorati";
                case WATER_AVAILABILITY_RISK -> "Suv Limit Xatari";
                case IRRIGATION_PLAN_RISK -> "Reja Ijro Xatari";
            };

            result.add(PredictionTypePerformanceDto.builder()
                    .predictionType(type)
                    .displayName(displayName)
                    .category(category)
                    .sampleSize(sample)
                    .isSufficientSample(sufficient)
                    .numericalMetrics(numMetrics)
                    .classificationMetrics(classMetrics)
                    .healthStatus(health.status())
                    .healthReasons(health.reasons())
                    .largestDeviations(largestDeviations)
                    .build());
        }

        return result;
    }
}
