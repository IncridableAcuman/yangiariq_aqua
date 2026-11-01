package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.intelligence.domain.port.IntelligenceWaterBudgetPort;
import com.aquaai.irrigation.domain.model.WaterBudget;
import com.aquaai.irrigation.domain.port.WaterBudgetDataPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IntelligenceWaterBudgetAdapter implements IntelligenceWaterBudgetPort {

    private final WaterBudgetDataPort waterBudgetDataPort;

    @Override
    public Optional<BigDecimal> getRemainingWaterBudgetM3(UUID fieldId) {
        if (fieldId == null) {
            return Optional.empty();
        }
        return waterBudgetDataPort.getWaterBudgetForField(fieldId)
                .map(WaterBudget::getRemainingAvailableM3);
    }
}
