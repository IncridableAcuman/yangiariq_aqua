package com.aquaai.ai.application;

import com.aquaai.ai.domain.AiProvider;
import com.aquaai.ai.domain.AiRecommendationRequest;
import com.aquaai.ai.domain.AiRecommendationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;

    public AiRecommendationResult getIrrigationAdvice(AiRecommendationRequest request) {
        log.info("Requesting irrigation recommendation via active provider: {}", aiProvider.getProviderName());
        return aiProvider.generateIrrigationRecommendation(request);
    }

    public String analyzeProblemReport(String description, String location) {
        log.info("Requesting water problem analysis via active provider: {}", aiProvider.getProviderName());
        return aiProvider.analyzeWaterProblem(description, location);
    }
}
