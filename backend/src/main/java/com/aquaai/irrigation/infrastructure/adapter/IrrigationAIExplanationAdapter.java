package com.aquaai.irrigation.infrastructure.adapter;

import com.aquaai.irrigation.domain.model.PlanTradeoff;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.IrrigationAIExplanationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IrrigationAIExplanationAdapter implements IrrigationAIExplanationPort {

    @Override
    public Optional<AIExplanationResult> generateExplanation(
            SmartIrrigationPlan plan,
            String cropType,
            String soilType,
            String irrigationMethod
    ) {
        if (plan == null) {
            return Optional.empty();
        }

        String crop = cropType != null ? cropType : "Field parcel";
        String soil = soilType != null ? soilType : "typical Khorezm loam";
        String method = irrigationMethod != null ? irrigationMethod : "standard furrow";

        String summary = String.format(
                "Irrigation turn planned for %s on %s: Recommended %s m³ over %d minutes using %s on %s soil.",
                crop,
                plan.getPlannedDate(),
                plan.getRecommendedVolumeM3(),
                plan.getEstimatedDurationMinutes(),
                method,
                soil
        );

        StringBuilder reasoning = new StringBuilder();
        reasoning.append(String.format("Agronomic calculation determined an optimal delivery of %s m³. ", plan.getRecommendedVolumeM3()));
        if (plan.getCurrentMoisturePercent() != null && plan.getTargetMoisturePercent() != null) {
            reasoning.append(String.format("Current root-zone moisture is %s%% against target %s%%. ",
                    plan.getCurrentMoisturePercent(), plan.getTargetMoisturePercent()));
        }
        if (plan.isBlocked()) {
            reasoning.append("CRITICAL: Plan is currently BLOCKED by hydraulic safety interlock. Valve actuation prohibited. ");
        } else {
            reasoning.append(String.format("Schedule priority is ranked %s. ", plan.getPriority()));
        }

        List<PlanTradeoff> tradeoffs = new ArrayList<>();
        if (plan.getRecommendedVolumeM3().doubleValue() > 100) {
            tradeoffs.add(PlanTradeoff.builder()
                    .id(UUID.randomUUID())
                    .category("APPLICATION_UNIFORMITY")
                    .description("High-volume irrigation should be monitored for tail-water runoff and uniform furrow advance.")
                    .build());
        }

        List<String> warnings = new ArrayList<>();
        if (plan.isBlocked()) {
            warnings.add("BLOCKED: " + plan.getBlockedReason());
        }
        for (var c : plan.getConstraints()) {
            if (c.isWarning()) {
                warnings.add(c.getMessage());
            }
        }

        return Optional.of(new AIExplanationResult(
                summary,
                reasoning.toString().trim(),
                tradeoffs,
                warnings
        ));
    }
}
