package com.aquaai.recommendation.infrastructure.ai;

import com.aquaai.recommendation.domain.model.AIRecommendationResponse;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.recommendation.domain.port.AgronomicAIProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic simulated advisory AI provider for offline development,
 * testing, and zero-downtime fallback when external LLM endpoints are unreachable.
 */
@Component
public class MockAgronomicAIAdapter implements AgronomicAIProvider {

    @Override
    public Optional<AIRecommendationResponse> generateRecommendation(AgronomicRecommendationContext context) {
        if (context == null) {
            return Optional.empty();
        }

        // 1. Critical hydraulic check
        if (context.hasAnomaly("SUSPECTED_PIPE_BURST") || context.hasAnomaly("WATER_PRESSURE_DROP")) {
            return Optional.of(AIRecommendationResponse.builder()
                    .recommendationType(RecommendationType.INSPECT_PIPE_BURST)
                    .priority(RecommendationPriority.CRITICAL)
                    .title("Urgent: Pipeline Integrity Breach Detected")
                    .summary("Abnormal pressure drop and flow divergence detected in field distribution network.")
                    .explanation("Telemetry corroborates hydraulic pressure drop and excessive flow rate. Irrigation must be suppressed to avoid massive water loss and soil erosion.")
                    .actions(List.of("Isolate sector valve immediately", "Dispatch canal technician for visual leak inspection"))
                    .doNotActions(List.of("Do not open secondary intake gates until pressure tests confirm line integrity"))
                    .limitations("Advisory derived from telemetry correlation; onsite physical confirmation required.")
                    .confidenceScore(BigDecimal.valueOf(0.92))
                    .providerName(getProviderName())
                    .build());
        }

        // 2. High rainfall check
        if (context.getWeather() != null && context.getWeather().hasSignificantRainfall(BigDecimal.valueOf(15.0))) {
            return Optional.of(AIRecommendationResponse.builder()
                    .recommendationType(RecommendationType.DELAY_IRRIGATION)
                    .priority(RecommendationPriority.HIGH)
                    .title("Postpone Scheduled Irrigation Due to Forecasted Rain")
                    .summary(String.format("Meteorological forecast indicates %s mm precipitation.", context.getWeather().getPrecipitationAmountMm()))
                    .explanation("Natural precipitation will satisfy near-term crop evapotranspiration requirements, preserving quota allocations.")
                    .actions(List.of("Suspend planned canal water release for 48 hours", "Re-evaluate soil moisture post-precipitation"))
                    .doNotActions(List.of("Do not apply soluble nitrogen fertilizer immediately prior to heavy runoff event"))
                    .limitations("Based on regional forecast probability; subject to atmospheric fluctuations.")
                    .confidenceScore(BigDecimal.valueOf(0.85))
                    .providerName(getProviderName())
                    .build());
        }

        // 3. Soil moisture deficit check
        if (context.getSoilMoisturePercent() != null && context.getSoilMoisturePercent().compareTo(BigDecimal.valueOf(18.0)) <= 0) {
            boolean isCritical = context.getSoilMoisturePercent().compareTo(BigDecimal.valueOf(15.0)) <= 0;
            return Optional.of(AIRecommendationResponse.builder()
                    .recommendationType(RecommendationType.IRRIGATE_NOW)
                    .priority(isCritical ? RecommendationPriority.CRITICAL : RecommendationPriority.HIGH)
                    .title(isCritical ? "Critical Soil Dehydration — Immediate Hydration Required" : "Crop Hydration Deficit Detected")
                    .summary(String.format("Volumetric soil moisture dropped to %s%%, reaching management allowable depletion limit.", context.getSoilMoisturePercent()))
                    .explanation(String.format("For crop %s on %s soil, available water capacity is critically depleted. Yield reduction risk increases without prompt water delivery.",
                            context.getCropType(), context.getSoilType()))
                    .actions(List.of(
                            "Initiate planned furrow or drip delivery within current allocation window",
                            "Target water application depth of 45-55 mm"
                    ))
                    .doNotActions(List.of("Do not exceed field capacity to prevent root hypoxia and waterlogging"))
                    .limitations("Soil moisture calibration assumes standard sensor depth of 30cm.")
                    .confidenceScore(BigDecimal.valueOf(0.88))
                    .providerName(getProviderName())
                    .build());
        }

        // 4. Salinity hazard check
        if (context.getSalinityDsM() != null && context.getSalinityDsM().compareTo(BigDecimal.valueOf(4.0)) >= 0) {
            return Optional.of(AIRecommendationResponse.builder()
                    .recommendationType(RecommendationType.MANAGE_SALINITY)
                    .priority(RecommendationPriority.HIGH)
                    .title("Elevated Root Zone Salinity — Salt Leaching Recommended")
                    .summary(String.format("Electrical conductivity measured at %s dS/m, exceeding safe agronomic baseline.", context.getSalinityDsM()))
                    .explanation("Secondary salinization impairs osmotic water uptake. Application of a 15-20% leaching fraction is recommended to flush soluble salts into drainage collectors.")
                    .actions(List.of(
                            "Calculate 15% leaching fraction in addition to normal irrigation quota",
                            "Verify operational capacity of collector drainage ditch"
                    ))
                    .doNotActions(List.of("Do not allow stagnant drainage ponding on parcel surface"))
                    .limitations("EC threshold is provisional based on regional Khorezm salinity classification.")
                    .confidenceScore(BigDecimal.valueOf(0.82))
                    .providerName(getProviderName())
                    .build());
        }

        // 5. Default healthy monitoring
        return Optional.of(AIRecommendationResponse.builder()
                .recommendationType(RecommendationType.MONITOR_SOIL_MOISTURE)
                .priority(RecommendationPriority.INFO)
                .title("Soil Moisture Within Optimal Agronomic Bounds")
                .summary("Soil moisture and root zone conditions are currently within target management range.")
                .explanation(String.format("Soil moisture is %s%%. Weather and transpiration trends do not warrant immediate intervention.",
                        context.getSoilMoisturePercent() != null ? context.getSoilMoisturePercent() + "%" : "stable"))
                .actions(List.of("Maintain routine sensor telemetry observation", "Check allocation quota status for next cycle"))
                .doNotActions(List.of("Do not apply unplanned supplementary irrigation"))
                .limitations("Continuous telemetry monitoring in effect.")
                .confidenceScore(BigDecimal.valueOf(0.80))
                .providerName(getProviderName())
                .build());
    }

    @Override
    public String getProviderName() {
        return "MOCK_AI_ADVISOR";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
