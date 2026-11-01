package com.aquaai.irrigation.domain.port;

import com.aquaai.irrigation.domain.model.PlanTradeoff;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;

import java.util.List;
import java.util.Optional;

/**
 * Domain port for synthesizing AI-assisted agronomic explanations, trade-off summaries,
 * and operator guidance.
 * <p>
 * Constraints Precedence:
 * AI cannot alter scheduled volumes, override hydraulic safety blocks, or bypass water budgets.
 */
public interface IrrigationAIExplanationPort {

    record AIExplanationResult(
            String summary,
            String reasoning,
            List<PlanTradeoff> tradeoffs,
            List<String> warnings
    ) {}

    Optional<AIExplanationResult> generateExplanation(
            SmartIrrigationPlan plan,
            String cropType,
            String soilType,
            String irrigationMethod
    );
}
