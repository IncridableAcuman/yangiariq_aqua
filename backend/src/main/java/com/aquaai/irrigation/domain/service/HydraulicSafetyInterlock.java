package com.aquaai.irrigation.domain.service;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import com.aquaai.irrigation.domain.model.PlanConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Domain safety interlock enforcing hydraulic constraints.
 * <p>
 * Core Principle:
 * If a pipe burst is suspected or critical pressure drop is detected, irrigation is STRICTLY BLOCKED.
 * AI cannot override this interlock.
 */
public class HydraulicSafetyInterlock {

    public record InterlockResult(
            boolean isBlocked,
            String blockedReason,
            List<PlanConstraint> constraints
    ) {}

    public InterlockResult evaluate(
            boolean hasSuspectedPipeBurst,
            boolean hasCriticalPressureDrop,
            BigDecimal currentPressureBar,
            BigDecimal currentFlowM3H
    ) {
        List<PlanConstraint> constraints = new ArrayList<>();

        if (hasSuspectedPipeBurst) {
            String msg = "Hydraulic Safety Interlock: SUSPECTED_PIPE_BURST anomaly active. Pressurization blocked to prevent catastrophic canal/pipe blowout.";
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.HYDRAULIC_SAFETY)
                    .status(ConstraintStatus.BLOCKED)
                    .message(msg)
                    .details("Active anomaly detects anomalous flow surge combined with pressure decline.")
                    .build());
            return new InterlockResult(true, msg, constraints);
        }

        if (hasCriticalPressureDrop) {
            String msg = "Hydraulic Safety Interlock: Critical pressure drop detected on distribution line. Irrigation blocked until pressure recovers.";
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.HYDRAULIC_SAFETY)
                    .status(ConstraintStatus.BLOCKED)
                    .message(msg)
                    .details("Line pressure fell below safe operational delivery minimum.")
                    .build());
            return new InterlockResult(true, msg, constraints);
        }

        // Warning-level check: low pressure (e.g. < 1.0 bar for pressurized systems)
        if (currentPressureBar != null && currentPressureBar.compareTo(BigDecimal.valueOf(1.0)) < 0 && currentPressureBar.compareTo(BigDecimal.ZERO) > 0) {
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.HYDRAULIC_SAFETY)
                    .status(ConstraintStatus.WARNING)
                    .message("Warning: Measured mainline pressure is low (" + currentPressureBar + " bar). Delivery uniformity may be impacted.")
                    .details("Verify upstream pump operation before dispatch.")
                    .build());
        } else {
            constraints.add(PlanConstraint.builder()
                    .id(UUID.randomUUID())
                    .type(ConstraintType.HYDRAULIC_SAFETY)
                    .status(ConstraintStatus.SATISFIED)
                    .message("Hydraulic line integrity verified normal. No pipe burst or pressure drop anomalies.")
                    .details("Normal system pressure and flow parameters observed.")
                    .build());
        }

        return new InterlockResult(false, null, constraints);
    }
}
