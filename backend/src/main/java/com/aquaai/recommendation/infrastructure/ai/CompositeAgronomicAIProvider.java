package com.aquaai.recommendation.infrastructure.ai;

import com.aquaai.recommendation.domain.model.AIRecommendationResponse;
import com.aquaai.recommendation.domain.model.AgronomicRecommendationContext;
import com.aquaai.recommendation.domain.port.AgronomicAIProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Composite AI provider with zero-downtime automatic fallback:
 * tries Gemini first if enabled/available, and seamlessly falls back to MockAgronomicAIAdapter.
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class CompositeAgronomicAIProvider implements AgronomicAIProvider {

    private final GeminiAgronomicAIAdapter geminiAdapter;
    private final MockAgronomicAIAdapter mockAdapter;

    @Override
    public Optional<AIRecommendationResponse> generateRecommendation(AgronomicRecommendationContext context) {
        if (geminiAdapter.isAvailable()) {
            try {
                Optional<AIRecommendationResponse> response = geminiAdapter.generateRecommendation(context);
                if (response.isPresent()) {
                    log.info("Successfully generated agronomic recommendation via Gemini for field {}", context.getFieldId());
                    return response;
                }
                log.warn("Gemini returned empty response for field {}. Falling back to Mock AI advisor.", context.getFieldId());
            } catch (Exception e) {
                log.warn("Gemini generation failed for field {}: {}. Falling back to Mock AI advisor.", context.getFieldId(), e.getMessage());
            }
        }

        return mockAdapter.generateRecommendation(context);
    }

    @Override
    public String getProviderName() {
        return geminiAdapter.isAvailable() ? geminiAdapter.getProviderName() : mockAdapter.getProviderName();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
