package com.aquaai.dashboard.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.field.domain.model.Field;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic priority scoring service computing parcel urgency (0-100),
 * human-readable explanation reasons, and concrete recommended next actions.
 * Safety interlock remains strictly authoritative.
 */
@Service
public class PriorityScoringService {

    public record ScoringResult(
            int score,
            IrrigationPriority priority,
            List<String> reasons,
            String recommendedAction,
            String actionTargetSection
    ) {}

    public ScoringResult evaluateFieldPriority(
            Field field,
            BigDecimal currentMoisturePercent,
            BigDecimal targetMoisturePercent,
            String telemetryFreshness,
            BigDecimal remainingWaterM3,
            boolean hydraulicSafetyBlocked,
            String hydraulicSafetyReason,
            List<Anomaly> activeAnomalies,
            List<AgronomicRecommendation> activeRecommendations,
            SmartIrrigationPlan activePlan
    ) {
        List<String> reasons = new ArrayList<>();

        // 1. Authoritative Safety Gate
        if (hydraulicSafetyBlocked) {
            reasons.add("CRITICAL: Hydraulic safety interlock active: pipe burst or pressure drop detected");
            if (hydraulicSafetyReason != null && !hydraulicSafetyReason.isBlank()) {
                reasons.add("Safety trigger: " + hydraulicSafetyReason);
            }
            reasons.add("Irrigation strictly blocked to protect distribution canal and pipe infrastructure");
            return new ScoringResult(
                    100,
                    IrrigationPriority.CRITICAL,
                    reasons,
                    "INVESTIGATE_SAFETY",
                    "anomalies"
            );
        }

        int score = 0;

        // 2. Soil Moisture Deficit
        BigDecimal target = targetMoisturePercent != null ? targetMoisturePercent : new BigDecimal("60.0");
        if (currentMoisturePercent != null) {
            BigDecimal gap = target.subtract(currentMoisturePercent);
            if (currentMoisturePercent.compareTo(new BigDecimal("15.0")) < 0) {
                score += 65;
                reasons.add("Critical drought: Soil moisture (" + currentMoisturePercent + "%) below wilting point (15%)");
            } else if (gap.compareTo(new BigDecimal("20.0")) >= 0) {
                score += 35;
                reasons.add("Severe soil moisture deficit: " + gap + "% below target (" + target + "%)");
            } else if (gap.compareTo(new BigDecimal("10.0")) >= 0) {
                score += 25;
                reasons.add("Moderate soil moisture deficit: " + gap + "% below target");
            } else if (gap.compareTo(BigDecimal.ZERO) > 0) {
                score += 10;
                reasons.add("Mild soil moisture deficit: " + gap + "% below target");
            } else {
                reasons.add("Soil moisture at or above optimal target (" + currentMoisturePercent + "%)");
            }
        } else {
            score += 15;
            reasons.add("Soil moisture telemetry not yet reported");
        }

        // 3. Crop Sensitivity
        String crop = field != null && field.getCropType() != null ? field.getCropType().name() : "GENERAL";
        switch (crop) {
            case "COTTON", "RICE" -> {
                score += 15;
                reasons.add("High crop water stress sensitivity (" + crop + ")");
            }
            case "VEGETABLE", "MELON" -> {
                score += 10;
                reasons.add("Medium crop sensitivity (" + crop + ")");
            }
            case "WHEAT", "MAIZE" -> {
                score += 5;
                reasons.add("Standard crop sensitivity (" + crop + ")");
            }
            default -> {}
        }

        // 4. Critical Anomalies
        boolean hasCriticalAnomaly = false;
        if (activeAnomalies != null) {
            for (Anomaly a : activeAnomalies) {
                if (a.getSeverity() == AnomalySeverity.CRITICAL) {
                    hasCriticalAnomaly = true;
                    score += 20;
                    reasons.add("Active CRITICAL anomaly alert: " + a.getAnomalyType());
                } else if (a.getSeverity() == AnomalySeverity.HIGH) {
                    score += 10;
                    reasons.add("Active HIGH anomaly alert: " + a.getAnomalyType());
                }
            }
        }

        // 5. Active Smart Irrigation Plan Status
        if (activePlan != null) {
            if (activePlan.getStatus() == IrrigationPlanStatus.PROPOSED) {
                score += 20;
                reasons.add("Smart irrigation plan awaiting operator/farmer approval");
            } else if (activePlan.getStatus() == IrrigationPlanStatus.SCHEDULED) {
                reasons.add("Smart plan approved and scheduled for execution");
            } else if (activePlan.getStatus() == IrrigationPlanStatus.IN_PROGRESS) {
                reasons.add("Irrigation delivery currently in progress");
            }
        }

        // 6. Water Budget Headroom
        if (remainingWaterM3 != null && remainingWaterM3.compareTo(BigDecimal.ZERO) <= 0) {
            reasons.add("Water quota depleted (0 m³ remaining available)");
        } else if (remainingWaterM3 != null) {
            reasons.add("Water quota available: " + remainingWaterM3.stripTrailingZeros().toPlainString() + " m³ remaining");
        }

        // 7. Telemetry Freshness
        if ("FRESH".equalsIgnoreCase(telemetryFreshness)) {
            reasons.add("Sensor telemetry is fresh and within operational SLA");
        } else if ("STALE".equalsIgnoreCase(telemetryFreshness)) {
            score += 10;
            reasons.add("Sensor telemetry is stale (>24h without telemetry report)");
        } else if ("MISSING".equalsIgnoreCase(telemetryFreshness)) {
            score += 15;
            reasons.add("No recent sensor telemetry available for parcel");
        }

        // Clamp final score
        score = Math.max(0, Math.min(100, score));
        IrrigationPriority priority = IrrigationPriority.fromScore(score);

        // 8. Resolve Concrete Action & Navigation Target
        String recommendedAction;
        String actionTargetSection;

        if (activePlan != null && activePlan.getStatus() == IrrigationPlanStatus.PROPOSED) {
            recommendedAction = "APPROVE_PLAN";
            actionTargetSection = "irrigation";
        } else if (hasCriticalAnomaly) {
            recommendedAction = "INVESTIGATE_ANOMALY";
            actionTargetSection = "anomalies";
        } else if (currentMoisturePercent != null && currentMoisturePercent.compareTo(new BigDecimal("15.0")) < 0) {
            if (activeRecommendations != null && !activeRecommendations.isEmpty()) {
                recommendedAction = "REVIEW_RECOMMENDATION";
                actionTargetSection = "ai-advisor";
            } else {
                recommendedAction = "GENERATE_PLAN";
                actionTargetSection = "irrigation";
            }
        } else if (remainingWaterM3 != null && remainingWaterM3.compareTo(BigDecimal.ZERO) <= 0
                && currentMoisturePercent != null && currentMoisturePercent.compareTo(new BigDecimal("30.0")) < 0) {
            recommendedAction = "REVIEW_WATER_ALLOCATION";
            actionTargetSection = "irrigation";
        } else if ("STALE".equalsIgnoreCase(telemetryFreshness) || "MISSING".equalsIgnoreCase(telemetryFreshness)) {
            recommendedAction = "INSPECT_SENSOR";
            actionTargetSection = "sensors";
        } else if (activeRecommendations != null && !activeRecommendations.isEmpty()) {
            recommendedAction = "REVIEW_RECOMMENDATION";
            actionTargetSection = "ai-advisor";
        } else {
            recommendedAction = "MONITOR";
            actionTargetSection = "fields";
        }

        return new ScoringResult(score, priority, reasons, recommendedAction, actionTargetSection);
    }
}
