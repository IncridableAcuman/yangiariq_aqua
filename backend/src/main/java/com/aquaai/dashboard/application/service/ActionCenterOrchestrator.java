package com.aquaai.dashboard.application.service;

import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.dashboard.api.dto.PriorityFieldDto;
import com.aquaai.dashboard.api.dto.UrgentActionDto;
import com.aquaai.field.domain.model.Field;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.sensor.domain.model.Sensor;
import com.aquaai.sensor.domain.model.SensorStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Derives actionable operational items directly from domain states.
 * Guarantees that every action has a clear, one-click destination and unambiguous verb.
 */
@Service
public class ActionCenterOrchestrator {

    private static final int MAX_URGENT_ACTIONS = 10;

    public List<UrgentActionDto> deriveUrgentActions(
            List<Field> fields,
            List<PriorityFieldDto> priorityFields,
            List<SmartIrrigationPlan> plans,
            List<Anomaly> activeAnomalies,
            List<AgronomicRecommendation> recommendations,
            List<Sensor> sensors
    ) {
        List<UrgentActionDto> actions = new ArrayList<>();
        Map<UUID, String> fieldNameMap = fields.stream()
                .collect(java.util.stream.Collectors.toMap(Field::getId, Field::getName, (a, b) -> a));

        // 1. Hydraulic Safety Blocks (CRITICAL)
        for (PriorityFieldDto pf : priorityFields) {
            if (pf.isHydraulicSafetyBlocked()) {
                actions.add(UrgentActionDto.builder()
                        .id("act-safety-" + pf.getFieldId())
                        .actionType("HYDRAULIC_SAFETY_BLOCK")
                        .severity("CRITICAL")
                        .title("Hydraulic Safety Interlock Active")
                        .description(pf.getHydraulicSafetyReason() != null
                                ? pf.getHydraulicSafetyReason()
                                : "Delivery pipe burst or line pressure collapse detected. Pressurization blocked.")
                        .fieldId(pf.getFieldId())
                        .fieldName(pf.getFieldName())
                        .targetSection("anomalies")
                        .actionLabel("Investigate Safety")
                        .actionPayload(Map.of("fieldId", pf.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }
        }

        // 2. Critical Drought Stress (<15% soil moisture)
        for (PriorityFieldDto pf : priorityFields) {
            if (!pf.isHydraulicSafetyBlocked() && pf.getCurrentMoisturePercent() != null
                    && pf.getCurrentMoisturePercent().compareTo(new BigDecimal("15.0")) < 0) {
                actions.add(UrgentActionDto.builder()
                        .id("act-drought-" + pf.getFieldId())
                        .actionType("CRITICAL_DROUGHT")
                        .severity("CRITICAL")
                        .title("Critical Soil Moisture Deficit")
                        .description("Root-zone moisture at " + pf.getCurrentMoisturePercent() + "%, below wilting threshold (15%). Crop damage imminent without hydration.")
                        .fieldId(pf.getFieldId())
                        .fieldName(pf.getFieldName())
                        .targetSection("irrigation")
                        .actionLabel("Review Irrigation")
                        .actionPayload(Map.of("fieldId", pf.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }
        }

        // 3. Smart Plans Awaiting Approval (HIGH)
        if (plans != null) {
            for (SmartIrrigationPlan plan : plans) {
                if (plan.getStatus() == IrrigationPlanStatus.PROPOSED) {
                    actions.add(UrgentActionDto.builder()
                            .id("act-plan-" + plan.getId())
                            .actionType("PLAN_APPROVAL_PENDING")
                            .severity("HIGH")
                            .title("Smart Irrigation Plan Awaiting Approval")
                            .description("Recommended volume: " + plan.getRecommendedVolumeM3() + " m³ scheduled for " + plan.getPlannedDate() + ". Human confirmation required.")
                            .fieldId(plan.getFieldId())
                            .fieldName(fieldNameMap.getOrDefault(plan.getFieldId(), "Field"))
                            .targetSection("irrigation")
                            .actionLabel("Review & Approve")
                            .actionPayload(Map.of("planId", plan.getId().toString()))
                            .createdAt(plan.getCreatedAt() != null ? plan.getCreatedAt() : Instant.now())
                            .build());
                }
            }
        }

        // 4. Critical & High Anomalies
        if (activeAnomalies != null) {
            for (Anomaly anomaly : activeAnomalies) {
                if (anomaly.getSeverity() == AnomalySeverity.CRITICAL || anomaly.getSeverity() == AnomalySeverity.HIGH) {
                    actions.add(UrgentActionDto.builder()
                            .id("act-anom-" + anomaly.getId())
                            .actionType("ACTIVE_ANOMALY")
                            .severity(anomaly.getSeverity().name())
                            .title("Anomaly Detected: " + anomaly.getAnomalyType())
                            .description(anomaly.getDescription() != null ? anomaly.getDescription() : "Active telemetry alert requires investigation.")
                            .fieldId(anomaly.getFieldId())
                            .fieldName(fieldNameMap.getOrDefault(anomaly.getFieldId(), "Field"))
                            .targetSection("anomalies")
                            .actionLabel("Review Anomaly")
                            .actionPayload(Map.of("anomalyId", anomaly.getId().toString()))
                            .createdAt(anomaly.getDetectedAt() != null ? anomaly.getDetectedAt() : Instant.now())
                            .build());
                }
            }
        }

        // 5. Water Budget Depleted with Moisture Deficit
        for (PriorityFieldDto pf : priorityFields) {
            if ("CRITICAL_OVER_CONSUMED".equals(pf.getWaterStatus())
                    || (pf.getRemainingM3() != null && pf.getRemainingM3().compareTo(BigDecimal.ZERO) <= 0
                    && pf.getCurrentMoisturePercent() != null && pf.getCurrentMoisturePercent().compareTo(new BigDecimal("40.0")) < 0)) {
                actions.add(UrgentActionDto.builder()
                        .id("act-quota-" + pf.getFieldId())
                        .actionType("WATER_BUDGET_EXHAUSTED")
                        .severity("HIGH")
                        .title("Water Quota Depleted")
                        .description("Parcel has zero remaining allocated water volume while soil moisture indicates irrigation requirement.")
                        .fieldId(pf.getFieldId())
                        .fieldName(pf.getFieldName())
                        .targetSection("irrigation")
                        .actionLabel("Review Allocation")
                        .actionPayload(Map.of("fieldId", pf.getFieldId().toString()))
                        .createdAt(Instant.now())
                        .build());
            }
        }

        // 6. Stale Telemetry or Offline Sensors (MEDIUM)
        if (sensors != null) {
            for (Sensor s : sensors) {
                if (s.getStatus() == SensorStatus.OFFLINE) {
                    actions.add(UrgentActionDto.builder()
                            .id("act-sensor-" + s.getId())
                            .actionType("SENSOR_OFFLINE")
                            .severity("MEDIUM")
                            .title("Sensor Device Offline")
                            .description("Sensor " + s.getDeviceIdentifier() + " (" + s.getType() + ") is unreachable.")
                            .fieldId(s.getFieldId())
                            .fieldName(fieldNameMap.getOrDefault(s.getFieldId(), "Field"))
                            .targetSection("sensors")
                            .actionLabel("Inspect Sensor")
                            .actionPayload(Map.of("sensorId", s.getId().toString()))
                            .createdAt(s.getLastSeenAt() != null ? s.getLastSeenAt() : Instant.now())
                            .build());
                }
            }
        }

        // 7. Active Unaccepted Agronomic Recommendations (MEDIUM)
        if (recommendations != null) {
            for (AgronomicRecommendation rec : recommendations) {
                if (rec.getPriority() == RecommendationPriority.CRITICAL || rec.getPriority() == RecommendationPriority.HIGH) {
                    actions.add(UrgentActionDto.builder()
                            .id("act-rec-" + rec.getId())
                            .actionType("RECOMMENDATION_READY")
                            .severity(rec.getPriority().name())
                            .title("Agronomic Recommendation: " + rec.getTitle())
                            .description(rec.getSummary() != null ? rec.getSummary() : "Advisory proposal ready for operator evaluation.")
                            .fieldId(rec.getFieldId())
                            .fieldName(fieldNameMap.getOrDefault(rec.getFieldId(), "Field"))
                            .targetSection("ai-advisor")
                            .actionLabel("Review Recommendation")
                            .actionPayload(Map.of("recommendationId", rec.getId().toString()))
                            .createdAt(rec.getGeneratedAt() != null ? rec.getGeneratedAt() : Instant.now())
                            .build());
                }
            }
        }

        // Deterministic Sort: CRITICAL (1), HIGH (2), MEDIUM (3), LOW (4), then by timestamp desc
        return actions.stream()
                .sorted(Comparator
                        .comparingInt((UrgentActionDto a) -> severityRank(a.getSeverity()))
                        .thenComparing(UrgentActionDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(MAX_URGENT_ACTIONS)
                .toList();
    }

    private int severityRank(String severity) {
        if (severity == null) return 5;
        return switch (severity.toUpperCase()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            case "LOW" -> 4;
            default -> 5;
        };
    }
}
