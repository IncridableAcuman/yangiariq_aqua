package com.aquaai.prediction.application.usecase;

import com.aquaai.prediction.application.dto.PredictionEvaluationDetailDto;
import com.aquaai.prediction.application.dto.PredictionEvaluationOverviewResponse;
import com.aquaai.prediction.application.dto.PredictionTypePerformanceDto;
import com.aquaai.prediction.domain.model.ClassificationEvaluationMetrics;
import com.aquaai.prediction.domain.model.EvaluationConfidenceTier;
import com.aquaai.prediction.domain.model.EvaluationDataQualityImpact;
import com.aquaai.prediction.domain.model.EvaluationDriftSummary;
import com.aquaai.prediction.domain.model.EvaluationInsight;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.HumanDecisionImpact;
import com.aquaai.prediction.domain.model.NumericalEvaluationMetrics;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionEvaluationRepository;
import com.aquaai.prediction.domain.port.PredictionRepository;
import com.aquaai.prediction.domain.service.EvaluationInsightGenerator;
import com.aquaai.prediction.domain.service.PredictionEvaluationAggregator;
import com.aquaai.prediction.domain.service.PredictionOutcomeMatcher;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetPredictionEvaluationOverviewUseCase {

    private final PredictionEvaluationRepository evaluationRepository;
    private final PredictionRepository predictionRepository;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;

    private final PredictionEvaluationAggregator aggregator = new PredictionEvaluationAggregator();
    private final EvaluationInsightGenerator insightGenerator = new EvaluationInsightGenerator();
    private final PredictionOutcomeMatcher outcomeMatcher = new PredictionOutcomeMatcher();

    @Transactional
    public PredictionEvaluationOverviewResponse execute(Instant from, Instant to) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        return execute(from, to, currentUserId, currentRole);
    }

    @Transactional
    public PredictionEvaluationOverviewResponse execute(
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
        Duration periodDuration = Duration.between(safeFrom, safeTo);
        Instant prevFrom = safeFrom.minus(periodDuration);
        Instant prevTo = safeFrom;

        // Auto-evaluate pending predictions for accessible fields where target has passed
        try {
            var pending = predictionRepository.findPendingEvaluations(safeTo);
            for (var p : pending) {
                if (fieldIds.contains(p.getFieldId()) && !evaluationRepository.existsByPredictionId(p.getId())) {
                    var eval = outcomeMatcher.matchAndEvaluate(p, dataProvider);
                    evaluationRepository.save(eval);
                    predictionRepository.save(p.recordOutcome(eval.getActualValue(), eval.getActualObservedAt(), null));
                }
            }
        } catch (Exception e) {
            log.warn("Background evaluation sync notice: {}", e.getMessage());
        }

        // Fetch evaluations for current period and previous period
        List<PredictionEvaluation> currentEvals = evaluationRepository.findEvaluationsForPeriod(fieldIds, safeFrom, safeTo);
        List<PredictionEvaluation> prevEvals = evaluationRepository.findEvaluationsForPeriod(fieldIds, prevFrom, prevTo);

        int totalEvaluated = (int) currentEvals.stream().filter(e -> e.getEvaluationStatus() == EvaluationStatus.EVALUATED).count();
        int pendingCount = (int) currentEvals.stream().filter(e -> e.getEvaluationStatus() == EvaluationStatus.PENDING).count();
        int insufficientCount = (int) currentEvals.stream().filter(e -> e.getEvaluationStatus() == EvaluationStatus.INSUFFICIENT_DATA).count();
        int interruptedCount = (int) currentEvals.stream().filter(e -> e.getEvaluationStatus() == EvaluationStatus.OUTCOME_INTERRUPTED).count();
        int totalPredictions = currentEvals.size();

        BigDecimal coveragePercent = BigDecimal.ZERO;
        if (totalPredictions > 0) {
            coveragePercent = BigDecimal.valueOf(totalEvaluated)
                    .divide(BigDecimal.valueOf(totalPredictions), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);
        }

        // Overall numerical and classification metrics
        NumericalEvaluationMetrics overallNum = aggregator.calculateNumericalMetrics(currentEvals);
        ClassificationEvaluationMetrics overallClass = aggregator.calculateClassificationMetrics(currentEvals);

        // Overall Model Health
        var healthResult = aggregator.determineModelHealth(overallNum, overallClass, totalEvaluated, PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE);

        // Drift Summary
        EvaluationDriftSummary drift = aggregator.calculateDrift(currentEvals, prevEvals, PredictionEvaluationAggregator.DEFAULT_MINIMUM_SAMPLE_SIZE);

        // Performance by Prediction Type
        List<PredictionTypePerformanceDto> typePerformance = new ArrayList<>();
        for (PredictionType type : PredictionType.values()) {
            List<PredictionTypePerformanceDto> perf = buildTypePerformance(type, currentEvals, fieldNameMap);
            typePerformance.addAll(perf);
        }

        // Confidence Calibration
        List<EvaluationConfidenceTier> confidenceTiers = aggregator.calculateConfidenceTiers(currentEvals);

        // Data Quality Impact
        List<EvaluationDataQualityImpact> dataQuality = aggregator.calculateDataQualityImpact(currentEvals);

        // Actionable Insights
        List<EvaluationInsight> insights = insightGenerator.generateInsights(currentEvals, overallNum, overallClass, confidenceTiers);

        // Human Decision Impact Audits
        List<HumanDecisionImpact> decisionAudits = buildHumanDecisionAudits(currentEvals, fieldNameMap);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(java.time.ZoneOffset.UTC);
        String periodLabel = String.format("%s — %s", dtf.format(safeFrom), dtf.format(safeTo));

        return PredictionEvaluationOverviewResponse.builder()
                .periodLabel(periodLabel)
                .from(safeFrom)
                .to(safeTo)
                .totalPredictionsGenerated(totalPredictions)
                .totalEvaluated(totalEvaluated)
                .pendingEvaluationsCount(pendingCount)
                .insufficientDataCount(insufficientCount)
                .interruptedCount(interruptedCount)
                .evaluationCoveragePercent(coveragePercent)
                .overallModelHealth(healthResult.status())
                .healthReasons(healthResult.reasons())
                .driftSummary(drift)
                .performanceByType(typePerformance)
                .confidenceCalibration(confidenceTiers)
                .dataQualityImpact(dataQuality)
                .actionableInsights(insights)
                .humanDecisionAudits(decisionAudits)
                .build();
    }

    private List<PredictionTypePerformanceDto> buildTypePerformance(
            PredictionType type,
            List<PredictionEvaluation> allEvaluations,
            Map<UUID, String> fieldNameMap
    ) {
        List<PredictionEvaluation> forType = allEvaluations.stream()
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

        return List.of(PredictionTypePerformanceDto.builder()
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

    private List<HumanDecisionImpact> buildHumanDecisionAudits(
            List<PredictionEvaluation> currentEvals,
            Map<UUID, String> fieldNameMap
    ) {
        return currentEvals.stream()
                .filter(e -> e.getPredictionType() == PredictionType.WATER_DEMAND_FORECAST || e.getRecommendedValue() != null)
                .limit(10)
                .map(e -> {
                    BigDecimal rec = e.getRecommendedValue();
                    BigDecimal app = e.getApprovedValue();
                    BigDecimal act = e.getActualValue();
                    BigDecimal decDelta = (app != null && rec != null) ? app.subtract(rec) : BigDecimal.ZERO;
                    BigDecimal outDelta = (act != null && app != null) ? act.subtract(app) : BigDecimal.ZERO;

                    return new HumanDecisionImpact(
                            e.getPredictionId(),
                            e.getFieldId(),
                            fieldNameMap.getOrDefault(e.getFieldId(), "Parcel"),
                            e.getTargetTime(),
                            e.getPredictedValue(),
                            rec,
                            app,
                            act,
                            decDelta,
                            outDelta,
                            e.getOperationalNotes()
                    );
                })
                .toList();
    }
}
