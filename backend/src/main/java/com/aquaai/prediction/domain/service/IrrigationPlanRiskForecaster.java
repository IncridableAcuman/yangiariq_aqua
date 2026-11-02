package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.UpcomingPlanInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain service evaluating operational and hydraulic risks for upcoming smart irrigation plans.
 * <p>
 * Critical Safety Invariant:
 * Prediction MUST NOT bypass the hydraulic safety interlock. If hydraulic hazards are active,
 * the operational risk is strictly BLOCKED.
 */
public class IrrigationPlanRiskForecaster {

    public Prediction forecast(
            FieldMetadata field,
            PredictionHorizon horizon,
            List<UpcomingPlanInfo> upcomingPlans,
            List<HydraulicAnomalyInfo> anomalies,
            WaterBalance waterBalance,
            SufficiencyEvaluation telemetrySufficiency,
            Instant generationTime
    ) {
        Instant now = generationTime != null ? generationTime : Instant.now();
        Instant targetTime = now.plus(horizon.getDuration());

        List<UpcomingPlanInfo> plansInHorizon = new ArrayList<>();
        if (upcomingPlans != null) {
            for (UpcomingPlanInfo p : upcomingPlans) {
                if (p.plannedStartAt() != null && !p.plannedStartAt().isAfter(targetTime)) {
                    plansInHorizon.add(p);
                }
            }
        }

        // 1. Check for ABSOLUTE HYDRAULIC SAFETY INTERLOCK
        boolean hasPipeBurst = false;
        boolean hasPressureDrop = false;
        String safetyHazardDetails = "";

        if (anomalies != null) {
            for (HydraulicAnomalyInfo a : anomalies) {
                if (!a.isResolved()) {
                    String type = a.anomalyType() != null ? a.anomalyType().toUpperCase() : "";
                    if (type.contains("PIPE_BURST") || type.contains("BURST")) {
                        hasPipeBurst = true;
                        safetyHazardDetails = "Suspected pipe burst active on parcel network.";
                    } else if (type.contains("PRESSURE_DROP") && "CRITICAL".equalsIgnoreCase(a.severity())) {
                        hasPressureDrop = true;
                        safetyHazardDetails = "Critical hydraulic pressure drop active.";
                    }
                }
            }
        }

        boolean anyPlanExplicitlyBlocked = plansInHorizon.stream().anyMatch(UpcomingPlanInfo::isBlocked);

        if (hasPipeBurst || hasPressureDrop || anyPlanExplicitlyBlocked) {
            String evidence = String.format(
                    "Hydraulic safety interlock active: %s %d upcoming plans within %s cannot execute safely.",
                    safetyHazardDetails.isBlank() ? "Plan explicitly locked by hydraulic interlock." : safetyHazardDetails,
                    plansInHorizon.size(),
                    horizon.getLabel()
            );
            return Prediction.create(
                    field.fieldId(),
                    PredictionType.IRRIGATION_PLAN_RISK,
                    horizon,
                    targetTime,
                    BigDecimal.valueOf(100.0), // 100% risk / blocked
                    BigDecimal.valueOf(100.0),
                    BigDecimal.valueOf(100.0),
                    "score",
                    95,
                    RiskLevel.BLOCKED,
                    DataSufficiency.SUFFICIENT,
                    null,
                    evidence,
                    "Strict safety interlock: AI or predictions cannot override active hydraulic locks.",
                    "Inspect hydraulic infrastructure and resolve safety anomaly in Operations Center.",
                    "operations"
            );
        }

        if (plansInHorizon.isEmpty()) {
            return Prediction.create(
                    field.fieldId(),
                    PredictionType.IRRIGATION_PLAN_RISK,
                    horizon,
                    targetTime,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "score",
                    90,
                    RiskLevel.LOW,
                    DataSufficiency.SUFFICIENT,
                    null,
                    "No upcoming irrigation plans scheduled within " + horizon.getLabel() + ".",
                    "Assumes no unscheduled manual events will be initiated.",
                    "Review irrigation planner if crop moisture indicates impending deficit.",
                    "irrigation"
            );
        }

        // 2. Evaluate Quota Headroom
        BigDecimal totalPlanVolume = plansInHorizon.stream()
                .map(p -> p.recommendedVolumeM3() != null ? p.recommendedVolumeM3() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingQuota = (waterBalance != null && waterBalance.remainingVolumeM3() != null)
                ? waterBalance.remainingVolumeM3()
                : BigDecimal.ZERO;

        boolean quotaExceeded = totalPlanVolume.compareTo(remainingQuota) > 0;

        // 3. Evaluate Telemetry Freshness
        boolean telemetryStale = telemetrySufficiency != null && telemetrySufficiency.sufficiency() == DataSufficiency.INSUFFICIENT;

        RiskLevel risk;
        String reason;
        String action;

        if (quotaExceeded) {
            risk = RiskLevel.HIGH;
            reason = String.format("Upcoming plans require %s m³ which exceeds remaining quota balance (%s m³).",
                    totalPlanVolume.toPlainString(), remainingQuota.toPlainString());
            action = "Adjust plan volume or request water quota transfer before start window.";
        } else if (telemetryStale) {
            risk = RiskLevel.MEDIUM;
            reason = "Field soil moisture telemetry is stale; post-irrigation verification and shutoff cannot be verified.";
            action = "Inspect soil moisture sensors prior to initiating scheduled irrigation.";
        } else {
            risk = RiskLevel.LOW;
            reason = String.format("%d upcoming plans scheduled with sufficient water quota and valid telemetry.", plansInHorizon.size());
            action = "Ensure canal gates and valves are prepared according to schedule.";
        }

        return Prediction.create(
                field.fieldId(),
                PredictionType.IRRIGATION_PLAN_RISK,
                horizon,
                targetTime,
                risk == RiskLevel.HIGH ? BigDecimal.valueOf(75.0) : risk == RiskLevel.MEDIUM ? BigDecimal.valueOf(45.0) : BigDecimal.valueOf(15.0),
                BigDecimal.ZERO,
                BigDecimal.valueOf(100.0),
                "score",
                85,
                risk,
                telemetrySufficiency != null ? telemetrySufficiency.sufficiency() : DataSufficiency.SUFFICIENT,
                null,
                reason,
                "Evaluation covers " + plansInHorizon.size() + " scheduled plans within " + horizon.getLabel() + ".",
                action,
                "operations"
        );
    }
}
