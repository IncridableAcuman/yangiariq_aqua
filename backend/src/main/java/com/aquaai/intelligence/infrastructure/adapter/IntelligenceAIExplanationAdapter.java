package com.aquaai.intelligence.infrastructure.adapter;

import com.aquaai.intelligence.domain.model.AgronomicAssessment;
import com.aquaai.intelligence.domain.port.IntelligenceAIExplanationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntelligenceAIExplanationAdapter implements IntelligenceAIExplanationPort {

    @Override
    public String generateAdvisoryExplanation(AgronomicAssessment assessment) {
        if (assessment == null) {
            return "Advisory explanation unavailable.";
        }

        if (assessment.isHydraulicSafetyBlocked()) {
            return "SAFETY ADVISORY: Mainline pipe burst or critical pressure drop detected. " +
                    "To prevent catastrophic erosion and pump cavitation, physical pressurization is blocked. " +
                    "Dispatch a field operator to inspect primary and secondary irrigation pipes.";
        }

        if (assessment.isWaterBudgetConstrained()) {
            return "QUOTA ADVISORY: Water allocation budget limit reached. " +
                    "All non-emergency watering must be suspended or restricted to prevent statutory overconsumption penalties.";
        }

        return switch (assessment.getPrimarySignal()) {
            case SOIL_TOO_DRY -> "AGRONOMIC ADVISORY: Soil moisture deficit is impacting root absorption. " +
                    "Schedule controlled hydration during cooler night or early morning hours to minimize evaporative loss.";
            case SOIL_TOO_WET -> "AGRONOMIC ADVISORY: Soil moisture is at field saturation. " +
                    "Withhold additional watering to preserve root aeration and prevent anaerobic soil conditions.";
            case HIGH_TEMPERATURE -> "METEOROLOGICAL ADVISORY: Severe atmospheric heat and garmsel conditions detected. " +
                    "Avoid daytime irrigation between 11:00 and 17:00 to prevent foliage scald and high evaporation.";
            case LOW_TEMPERATURE -> "FROST HAZARD ADVISORY: Near-freezing temperatures observed. " +
                    "Avoid overhead watering during freezing periods to protect crop tissue.";
            case NO_RECENT_TELEMETRY -> "HARDWARE ADVISORY: Missing or invalidated sensor telemetry readings. " +
                    "Verify physical probe contact, solar battery charge, and telemetry transmission before altering irrigation schedule.";
            default -> "OPERATIONAL ADVISORY: Monitored field conditions remain within acceptable agronomic bounds.";
        };
    }
}
