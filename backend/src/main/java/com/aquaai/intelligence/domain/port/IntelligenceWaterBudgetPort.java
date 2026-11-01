package com.aquaai.intelligence.domain.port;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for querying volumetric water budget constraints.
 */
public interface IntelligenceWaterBudgetPort {

    Optional<BigDecimal> getRemainingWaterBudgetM3(UUID fieldId);
}
