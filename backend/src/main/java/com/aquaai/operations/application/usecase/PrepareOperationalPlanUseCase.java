package com.aquaai.operations.application.usecase;

import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.irrigation.application.usecase.GenerateSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import com.aquaai.irrigation.domain.model.PlanConstraint;
import com.aquaai.irrigation.domain.model.PlanTradeoff;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import com.aquaai.operations.api.dto.PreparePlanRequest;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Prepares, derives, or customizes a smart irrigation plan derived from active agronomic advice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrepareOperationalPlanUseCase {

    private final FieldOwnershipChecker fieldOwnershipChecker;
    private final GenerateSmartIrrigationPlanUseCase generatePlanUseCase;
    private final SmartIrrigationPlanRepository planRepository;
    private final WaterBudgetDataPort waterBudgetDataPort;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public SmartIrrigationPlan execute(UUID fieldId, PreparePlanRequest request) {
        return execute(
                fieldId,
                request,
                currentUserProvider.getRequiredUserId(),
                currentUserProvider.getRequiredUserRole()
        );
    }

    @Transactional
    public SmartIrrigationPlan execute(UUID fieldId, PreparePlanRequest request, UUID currentUserId, UserRole currentRole) {
        // Enforce anti-IDOR
        fieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole);

        LocalDate targetDate = request != null && request.getTargetDate() != null ? request.getTargetDate() : LocalDate.now();
        BigDecimal customVolume = request != null ? request.getCustomVolumeM3() : null;

        // Check if an active plan already exists
        Optional<SmartIrrigationPlan> activeOpt = planRepository.findActiveByFieldId(fieldId);
        SmartIrrigationPlan plan;

        if (activeOpt.isPresent()) {
            plan = activeOpt.get();
        } else {
            plan = generatePlanUseCase.execute(fieldId, targetDate);
        }

        // Apply custom planned volume if specified by operator
        if (customVolume != null && customVolume.compareTo(BigDecimal.ZERO) > 0) {
            Optional<WaterBudget> budgetOpt = waterBudgetDataPort.getWaterBudgetForField(fieldId);
            if (budgetOpt.isPresent()) {
                WaterBudget budget = budgetOpt.get();
                if (customVolume.compareTo(budget.getRemainingAvailableM3()) > 0) {
                    throw new BusinessRuleException(String.format(
                            "Cannot set planned volume to %s m³: Exceeds remaining seasonal quota of %s m³.",
                            customVolume, budget.getRemainingAvailableM3()
                    ));
                }
            }

            BigDecimal diff = customVolume.subtract(plan.getRecommendedVolumeM3());
            BigDecimal pct = plan.getRecommendedVolumeM3().compareTo(BigDecimal.ZERO) > 0 ?
                    diff.divide(plan.getRecommendedVolumeM3(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

            String sign = diff.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            String deviationDesc = String.format("Operator adjusted volume from %s m³ to %s m³ (%s%s m³, %s%s%%).",
                    plan.getRecommendedVolumeM3(), customVolume, sign, diff, sign, pct.setScale(1, RoundingMode.HALF_UP));

            plan.addTradeoff(PlanTradeoff.builder()
                    .id(UUID.randomUUID())
                    .category("OPERATOR_VOLUME_CUSTOMIZATION")
                    .description(deviationDesc)
                    .build());

            plan.addConstraint(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.WATER_QUOTA)
                    .status(ConstraintStatus.WARNING)
                    .message(deviationDesc)
                    .details(request != null && request.getCustomNotes() != null ? request.getCustomNotes() : "Manual volume override by operator")
                    .build());

            plan = planRepository.save(plan);
        }

        return plan;
    }
}
