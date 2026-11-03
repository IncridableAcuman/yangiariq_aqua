package com.aquaai.prediction.application.dto;

import java.util.UUID;

public record SelectAdaptiveMethodRequest(
        UUID fieldId,
        String predictionType
) {}
