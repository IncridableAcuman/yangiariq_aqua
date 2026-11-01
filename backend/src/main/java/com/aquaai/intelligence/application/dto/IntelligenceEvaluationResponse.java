package com.aquaai.intelligence.application.dto;

public record IntelligenceEvaluationResponse(
        AgronomicAssessmentResponse assessment,
        IntelligenceRecommendationResponse recommendation
) {}
