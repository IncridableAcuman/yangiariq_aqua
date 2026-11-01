package com.aquaai.intelligence.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.intelligence.application.dto.AgronomicAssessmentResponse;
import com.aquaai.intelligence.application.dto.IntelligenceEvaluationResponse;
import com.aquaai.intelligence.application.dto.IntelligenceRecommendationResponse;
import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.model.IntelligenceRecommendation;
import com.aquaai.intelligence.domain.model.TelemetrySignal;
import com.aquaai.intelligence.domain.model.TelemetryTrend;
import com.aquaai.intelligence.domain.port.AgronomicAssessmentRepository;
import com.aquaai.intelligence.domain.port.IntelligenceAnomalyPort;
import com.aquaai.intelligence.domain.port.IntelligenceFieldPort;
import com.aquaai.intelligence.domain.port.IntelligenceHydraulicSafetyPort;
import com.aquaai.intelligence.domain.port.IntelligenceTelemetryPort;
import com.aquaai.intelligence.domain.port.IntelligenceWaterBudgetPort;
import com.aquaai.intelligence.domain.service.AgronomicAssessmentEngine;
import com.aquaai.intelligence.domain.service.FreshnessEvaluator;
import com.aquaai.intelligence.domain.service.IntelligenceRecommendationEngine;
import com.aquaai.intelligence.domain.service.TelemetryNormalizationService;
import com.aquaai.intelligence.domain.service.TemporalTrendCalculator;
import com.aquaai.sensor.domain.model.SensorReading;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates full telemetry normalization, trend calculation, deterministic agronomic assessment,
 * and safe decision-support recommendation generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluateFieldIntelligenceUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final IntelligenceFieldPort fieldPort;
    private final IntelligenceTelemetryPort telemetryPort;
    private final IntelligenceAnomalyPort anomalyPort;
    private final IntelligenceHydraulicSafetyPort hydraulicSafetyPort;
    private final IntelligenceWaterBudgetPort waterBudgetPort;
    private final AgronomicAssessmentRepository assessmentRepository;

    private final FreshnessEvaluator freshnessEvaluator = new FreshnessEvaluator();
    private final TelemetryNormalizationService normalizationService = new TelemetryNormalizationService(freshnessEvaluator);
    private final TemporalTrendCalculator trendCalculator = new TemporalTrendCalculator();
    private final AgronomicAssessmentEngine assessmentEngine = new AgronomicAssessmentEngine();
    private final IntelligenceRecommendationEngine recommendationEngine = new IntelligenceRecommendationEngine();

    @Transactional
    public IntelligenceEvaluationResponse execute(UUID fieldId) {
        UUID currentUserId = currentUserProvider.getRequiredUserId();
        UserRole currentRole = currentUserProvider.getRequiredUserRole();

        // Anti-IDOR security check: throws ResourceNotFoundException (404) for unauthorized farmers
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        Instant now = Instant.now();

        // 1. Retrieve field agronomic metadata
        var parcelDetails = fieldPort.getFieldDetails(fieldId).orElse(null);
        BigDecimal fieldArea = parcelDetails != null ? parcelDetails.areaHectares() : BigDecimal.ONE;

        // 2. Retrieve latest physical readings and recent trend history
        List<SensorReading> latestReadings = telemetryPort.getLatestReadings(fieldId);
        List<TelemetrySignal> normalizedSignals = normalizationService.normalize(latestReadings, now);

        Instant twelveHoursAgo = now.minus(12, ChronoUnit.HOURS);
        List<SensorReading> moistureHistory = telemetryPort.getRecentReadings(
                fieldId,
                SensorType.SOIL_MOISTURE,
                twelveHoursAgo,
                now
        );

        List<TemporalTrendCalculator.DataPoint> trendPoints = new ArrayList<>();
        if (moistureHistory != null) {
            for (SensorReading r : moistureHistory) {
                if (r != null && r.getValue() != null && r.getMeasuredAt() != null) {
                    trendPoints.add(new TemporalTrendCalculator.DataPoint(r.getValue(), r.getMeasuredAt()));
                }
            }
        }
        TelemetryTrend moistureTrend = trendCalculator.calculateTrend(trendPoints);

        // 3. Query active anomaly state
        var anomalyStatus = anomalyPort.getActiveAnomalyStatus(fieldId);
        boolean hasPipeBurst = anomalyStatus != null && anomalyStatus.hasPipeBurst();
        boolean hasPressureDrop = anomalyStatus != null && anomalyStatus.hasCriticalPressureDrop();

        // 4. Query hydraulic safety interlock
        var hydraulicSafety = hydraulicSafetyPort.checkHydraulicSafety(fieldId);
        if (hydraulicSafety != null && hydraulicSafety.isBlocked()) {
            hasPipeBurst = true;
        }

        // 5. Query remaining volumetric water budget
        BigDecimal remainingBudget = waterBudgetPort.getRemainingWaterBudgetM3(fieldId).orElse(null);

        // 6. Run deterministic agronomic assessment
        AgronomicAssessmentEngine.AssessmentInput input = new AgronomicAssessmentEngine.AssessmentInput(
                fieldId,
                normalizedSignals,
                moistureTrend,
                hasPipeBurst,
                hasPressureDrop,
                remainingBudget,
                now
        );
        AgronomicAssessment assessment = assessmentEngine.assess(input);

        // 7. Persist assessment audit record
        AgronomicAssessment savedAssessment = assessmentRepository.save(assessment);

        // 8. Generate safe human decision-support recommendation
        IntelligenceRecommendation recommendation = recommendationEngine.generate(savedAssessment, fieldArea);

        log.info("Evaluated intelligence for field {}: primarySignal={}, severity={}, blocked={}, budgetConstrained={}",
                fieldId, savedAssessment.getPrimarySignal(), savedAssessment.getOverallSeverity(),
                savedAssessment.isHydraulicSafetyBlocked(), savedAssessment.isWaterBudgetConstrained());

        return new IntelligenceEvaluationResponse(
                AgronomicAssessmentResponse.fromDomain(savedAssessment),
                IntelligenceRecommendationResponse.fromDomain(recommendation)
        );
    }
}
