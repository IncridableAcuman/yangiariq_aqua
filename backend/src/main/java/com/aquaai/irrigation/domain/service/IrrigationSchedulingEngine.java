package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.IrrigationRequirement;
import com.aquaai.irrigation.domain.model.PlanConstraint;
import com.aquaai.irrigation.domain.model.PlanTradeoff;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deterministic constraint-based scheduling engine for smart parcel irrigation.
 * <p>
 * Evaluates water quotas, hydraulic safety, weather conditions, nocturnal window optimization,
 * and produces a fully explainable, constraint-checked {@link SmartIrrigationPlan}.
 */
public class IrrigationSchedulingEngine {

    private final HydraulicSafetyInterlock safetyInterlock;
    private final IrrigationPriorityCalculator priorityCalculator;

    public IrrigationSchedulingEngine(
            HydraulicSafetyInterlock safetyInterlock,
            IrrigationPriorityCalculator priorityCalculator
    ) {
        this.safetyInterlock = safetyInterlock != null ? safetyInterlock : new HydraulicSafetyInterlock();
        this.priorityCalculator = priorityCalculator != null ? priorityCalculator : new IrrigationPriorityCalculator();
    }

    public SmartIrrigationPlan schedulePlan(
            UUID fieldId,
            UUID waterAllocationId,
            IrrigationRequirement requirement,
            WaterBudget waterBudget,
            LocalDate targetDate,
            ZoneId zoneId,
            boolean hasSuspectedPipeBurst,
            boolean hasCriticalPressureDrop,
            BigDecimal currentPressureBar,
            BigDecimal currentFlowM3H,
            BigDecimal currentMoisturePercent,
            BigDecimal targetMoisturePercent,
            String cropType,
            String irrigationMethod,
            BigDecimal ambientTemperatureCelsius,
            BigDecimal forecastRainMm,
            BigDecimal windSpeedMps
    ) {
        ZoneId zone = zoneId != null ? zoneId : ZoneId.of("Asia/Tashkent");
        LocalDate date = targetDate != null ? targetDate : LocalDate.now(zone);
        List<PlanConstraint> constraints = new ArrayList<>();
        List<PlanTradeoff> tradeoffs = new ArrayList<>();

        // 1. Evaluate Hydraulic Safety Interlock (Level 1: Absolute Safety)
        HydraulicSafetyInterlock.InterlockResult safetyResult = safetyInterlock.evaluate(
                hasSuspectedPipeBurst,
                hasCriticalPressureDrop,
                currentPressureBar,
                currentFlowM3H
        );
        constraints.addAll(safetyResult.constraints());

        // 2. Evaluate Water Budget & Quota Constraints (Level 2: Hard Water Constraints)
        BigDecimal requestedVolume = requirement.getGrossVolumeM3();
        BigDecimal scheduledVolume = requestedVolume;
        boolean waterShortage = false;

        if (waterBudget != null) {
            if (!waterBudget.canFulfill(requestedVolume)) {
                waterShortage = true;
                BigDecimal shortage = waterBudget.calculateShortage(requestedVolume);
                scheduledVolume = waterBudget.getRemainingAvailableM3();

                constraints.add(PlanConstraint.builder()
                        .id(UUID.randomUUID())
                        .type(ConstraintType.WATER_QUOTA)
                        .status(scheduledVolume.compareTo(BigDecimal.ZERO) > 0 ? ConstraintStatus.WARNING : ConstraintStatus.VIOLATED)
                        .message("Water budget shortage: Required " + requestedVolume + " m³, but remaining allocation is " + waterBudget.getRemainingAvailableM3() + " m³.")
                        .details("Plan volume capped at remaining quota (" + scheduledVolume + " m³). Deficit: " + shortage + " m³.")
                        .build());

                tradeoffs.add(PlanTradeoff.builder()
                        .id(UUID.randomUUID())
                        .category("WATER_QUOTA_ADJUSTMENT")
                        .description("Volume scaled down from " + requestedVolume + " m³ to " + scheduledVolume + " m³ to prevent seasonal quota overdraft.")
                        .build());
            } else {
                constraints.add(PlanConstraint.builder()
                        .id(UUID.randomUUID())
                        .type(ConstraintType.WATER_QUOTA)
                        .status(ConstraintStatus.SATISFIED)
                        .message("Water allocation verified: Sufficient quota available (" + waterBudget.getRemainingAvailableM3() + " m³ remaining).")
                        .details("Requested volume " + requestedVolume + " m³ fits within seasonal allocation.")
                        .build());
            }
        }

        // 3. Evaluate Weather Conditions & Windows (Level 5: Weather Optimization)
        boolean rainDelay = false;
        if (forecastRainMm != null && forecastRainMm.compareTo(BigDecimal.valueOf(15.0)) >= 0) {
            rainDelay = true;
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.WEATHER_WINDOW)
                    .status(ConstraintStatus.WARNING)
                    .message("Weather alert: Substantial precipitation (" + forecastRainMm + " mm) forecast. Irrigation may be delayed or cancelled.")
                    .details("Effective rain is expected to replenish root-zone moisture naturally.")
                    .build());
            tradeoffs.add(PlanTradeoff.builder()
                    .id(UUID.randomUUID())
                    .category("PRECIPITATION_OFFSET")
                    .description("Consider deferring irrigation turn by 24h to utilize natural rainfall.")
                    .build());
        }

        // Sprinkler wind check
        if (irrigationMethod != null && irrigationMethod.equalsIgnoreCase("SPRINKLER") && windSpeedMps != null && windSpeedMps.compareTo(BigDecimal.valueOf(7.0)) > 0) {
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.WEATHER_WINDOW)
                    .status(ConstraintStatus.WARNING)
                    .message("High wind speed (" + windSpeedMps + " m/s) detected for sprinkler system.")
                    .details("Wind drift will reduce distribution uniformity. Operate during calm nocturnal window.")
                    .build());
        }

        // 4. Determine Optimal Scheduling Window (Prefer nocturnal if hot or heat-sensitive)
        boolean preferNocturnal = (ambientTemperatureCelsius != null && ambientTemperatureCelsius.compareTo(BigDecimal.valueOf(32.0)) >= 0)
                || (irrigationMethod != null && irrigationMethod.equalsIgnoreCase("SPRINKLER"));

        LocalTime startTime = preferNocturnal ? LocalTime.of(22, 0) : LocalTime.of(6, 0);
        ZonedDateTime startZdt = date.atTime(startTime).atZone(zone);
        Instant plannedStartAt = startZdt.toInstant();

        // Calculate duration based on scheduled volume
        int scheduledDurationMinutes = requirement.getEstimatedDurationMinutes();
        if (waterShortage && requirement.getFlowRateM3H().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hours = scheduledVolume.divide(requirement.getFlowRateM3H(), 4, RoundingMode.HALF_UP);
            scheduledDurationMinutes = (int) Math.round(hours.multiply(BigDecimal.valueOf(60.0)).doubleValue());
        }
        scheduledDurationMinutes = Math.max(15, Math.min(scheduledDurationMinutes, 1440));
        Instant plannedEndAt = plannedStartAt.plus(scheduledDurationMinutes, ChronoUnit.MINUTES);

        if (preferNocturnal) {
            tradeoffs.add(PlanTradeoff.builder()
                    .id(UUID.randomUUID())
                    .category("NOCTURNAL_DISPATCH")
                    .description("Scheduled for night dispatch (starting 22:00) to minimize evaporative transpiration losses.")
                    .build());
        }

        // 5. Calculate Priority
        IrrigationPriorityCalculator.PriorityResult priorityResult = priorityCalculator.calculatePriority(
                currentMoisturePercent,
                targetMoisturePercent,
                cropType,
                false,
                ambientTemperatureCelsius,
                forecastRainMm,
                waterBudget != null ? waterBudget.getRemainingAvailableM3() : null,
                scheduledVolume,
                com.aquaai.irrigation.domain.model.DataQualityGate.SUFFICIENT
        );

        // 6. Build Synthesized Reason & Explanation
        String reason = buildExplanationReason(
                cropType,
                currentMoisturePercent,
                targetMoisturePercent,
                scheduledVolume,
                priorityResult.priority(),
                safetyResult.isBlocked(),
                waterShortage
        );

        String summary = String.format(
                "Irrigation turn for %s on %s: Recommended %s m³ over %d mins. Priority: %s.",
                cropType != null ? cropType : "parcel",
                date,
                scheduledVolume,
                scheduledDurationMinutes,
                priorityResult.priority()
        );

        Instant now = Instant.now();
        SmartIrrigationPlan plan = new SmartIrrigationPlan(
                UUID.randomUUID(),
                fieldId,
                waterAllocationId,
                safetyResult.isBlocked() ? IrrigationPlanStatus.DRAFT : IrrigationPlanStatus.PROPOSED,
                priorityResult.priority(),
                date,
                plannedStartAt,
                plannedEndAt,
                scheduledVolume,
                scheduledDurationMinutes,
                requirement.getFlowRateM3H(),
                targetMoisturePercent,
                currentMoisturePercent,
                "HIGH",
                BigDecimal.valueOf(0.85),
                reason,
                summary,
                null,
                safetyResult.isBlocked(),
                safetyResult.blockedReason(),
                null,
                null,
                "DETERMINISTIC_SCHEDULER",
                now,
                now,
                null,
                constraints,
                tradeoffs
        );

        return plan;
    }

    private String buildExplanationReason(
            String cropType,
            BigDecimal currentMoisture,
            BigDecimal targetMoisture,
            BigDecimal volumeM3,
            IrrigationPriority priority,
            boolean isBlocked,
            boolean waterShortage
    ) {
        if (isBlocked) {
            return "Irrigation plan generation complete but EXECUTION BLOCKED by hydraulic safety interlock.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Scheduled %s m³ based on root-zone deficit for %s. ", volumeM3, cropType != null ? cropType : "parcel"));
        if (currentMoisture != null && targetMoisture != null) {
            sb.append(String.format("Current moisture: %s%%, Target: %s%%. ", currentMoisture, targetMoisture));
        }
        if (waterShortage) {
            sb.append("Plan was volume-constrained by available seasonal water quota. ");
        }
        sb.append("Priority: ").append(priority).append(".");
        return sb.toString();
    }
}
