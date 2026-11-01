package com.aquaai.recommendation.api.dto;

import com.aquaai.recommendation.domain.model.RecommendedAction;

import java.util.UUID;

public record RecommendedActionResponse(
        UUID id,
        int actionOrder,
        String actionText,
        String urgency,
        boolean isNegativeAction
) {
    public static RecommendedActionResponse fromDomain(RecommendedAction action) {
        if (action == null) {
            return null;
        }
        return new RecommendedActionResponse(
                action.getId(),
                action.getActionOrder(),
                action.getActionText(),
                action.getUrgency(),
                action.isNegativeAction()
        );
    }
}
