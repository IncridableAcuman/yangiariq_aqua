package com.aquaai.irrigation.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.Field;
import com.aquaai.irrigation.domain.model.IrrigationRequirement;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.AgronomicContextPort;
import com.aquaai.irrigation.domain.port.AnomalyDataPort;
import com.aquaai.irrigation.domain.port.FieldDataPort;
import com.aquaai.irrigation.domain.port.IrrigationAIExplanationPort;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.TelemetryDataPort;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.irrigation.domain.service.HydraulicSafetyInterlock;
import com.aquaai.irrigation.domain.service.IrrigationPriorityCalculator;
import com.aquaai.irrigation.domain.service.IrrigationRequirementCalculator;
import com.aquaai.irrigation.domain.service.IrrigationSchedulingEngine;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerateSmartIrrigationPlanUseCase {

    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final CurrentUserProvider currentUserProvider;
    private final FieldDataPort fieldDataPort;
    private final TelemetryDataPort telemetryDataPort;
    private final AnomalyDataPort anomalyDataPort;
    private final AgronomicContextPort agronomicContextPort;
    private final WaterBudgetDataPort waterBudgetDataPort;
    private final SmartIrrigationPlanRepository planRepository;
    private final IrrigationAIExplanationPort aiExplanationPort;

    private final IrrigationRequirementCalculator requirementCalculator = new IrrigationRequirementCalculator();
    private final IrrigationSchedulingEngine schedulingEngine = new IrrigationSchedulingEngine(
            new HydraulicSafetyInterlock(),
            new IrrigationPriorityCalculator()
    );

    @Transactional
    public SmartIrrigationPlan execute(UUID fieldId, LocalDate targetDate) {
        return execute(
                fieldId,
                targetDate,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID fieldId, LocalDate targetDate, UUID currentUserId, UserRole currentRole) {
        // 1. Anti-IDOR Authorization Check (Throws 404 if unauthorized farmer)
        Field field = fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        ZoneId zone = ZoneId.of("Asia/Tashkent");
        LocalDate date = targetDate != null ? targetDate : LocalDate.now(zone);

        // 2. Idempotency / Deduplication: If an active plan already exists for this date, return it
        Optional<SmartIrrigationPlan> activePlan = planRepository.findActiveByFieldId(fieldId);
        if (activePlan.isPresent() && activePlan.get().getPlannedDate().equals(date)) {
            log.info("Returning existing active smart irrigation plan {} for field {} on date {}",
                    activePlan.get().getId(), fieldId, date);
            return activePlan.get();
        }

        // 3. Gather Domain Data
        FieldDataPort.FieldAgronomicData fieldData = fieldDataPort.getFieldData(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));

        TelemetryDataPort.ParcelTelemetryData telemetry = telemetryDataPort.getLatestTelemetry(fieldId).orElse(null);
        AnomalyDataPort.ParcelAnomalyStatus anomalyStatus = anomalyDataPort.getAnomalyStatus(fieldId);
        AgronomicContextPort.MeteorologicalData meteo = agronomicContextPort.getMeteorologicalData(fieldId);
        WaterBudget waterBudget = waterBudgetDataPort.getWaterBudgetForField(fieldId).orElse(null);
        UUID allocationId = waterBudgetDataPort.getActiveWaterAllocationId(fieldId).orElse(null);

        // 4. Calculate Net & Gross Irrigation Requirement
        IrrigationRequirement requirement = requirementCalculator.calculate(
                telemetry != null ? telemetry.soilMoisturePercent() : null,
                null,
                fieldData.cropType(),
                fieldData.soilType(),
                fieldData.irrigationMethod(),
                fieldData.areaHectares(),
                meteo != null ? meteo.referenceEt0MmDay() : null,
                meteo != null ? meteo.forecastRainMm() : null
        );

        // 5. Run Deterministic Scheduling Engine
        SmartIrrigationPlan plan = schedulingEngine.schedulePlan(
                fieldId,
                allocationId,
                requirement,
                waterBudget,
                date,
                zone,
                anomalyStatus.hasSuspectedPipeBurst(),
                anomalyStatus.hasCriticalPressureDrop(),
                telemetry != null ? telemetry.pressureBar() : null,
                telemetry != null ? telemetry.waterFlowM3H() : null,
                telemetry != null ? telemetry.soilMoisturePercent() : null,
                null,
                fieldData.cropType(),
                fieldData.irrigationMethod(),
                meteo != null ? meteo.temperatureCelsius() : null,
                meteo != null ? meteo.forecastRainMm() : null,
                meteo != null ? meteo.windSpeedMps() : null
        );

        // 6. Enrich with Advisory AI Explanation (if available)
        try {
            var aiResult = aiExplanationPort.generateExplanation(
                    plan,
                    fieldData.cropType(),
                    fieldData.soilType(),
                    fieldData.irrigationMethod()
            );
            if (aiResult.isPresent()) {
                var explanation = aiResult.get();
                for (var tradeoff : explanation.tradeoffs()) {
                    plan.addTradeoff(tradeoff);
                }
            }
        } catch (Exception e) {
            log.warn("AI explanation provider encountered error; falling back to deterministic explanation: {}", e.getMessage());
        }

        // 7. Persist and Return
        return planRepository.save(plan);
    }
}
