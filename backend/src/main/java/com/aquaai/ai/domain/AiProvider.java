package com.aquaai.ai.domain;

public interface AiProvider {

    String getProviderName();

    AiRecommendationResult generateIrrigationRecommendation(AiRecommendationRequest request);

    String analyzeWaterProblem(String problemDescription, String districtLocation);
}
