package com.aquaai.recommendation.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Concrete actionable task associated with an agronomic recommendation.
 */
@Getter
public class RecommendedAction {

    private final UUID id;
    private final int actionOrder;
    private final String actionText;
    private final String urgency;
    private final boolean isNegativeAction;

    public RecommendedAction(UUID id, int actionOrder, String actionText, String urgency, boolean isNegativeAction) {
        this.id = id != null ? id : UUID.randomUUID();
        this.actionOrder = actionOrder;
        this.actionText = Objects.requireNonNull(actionText, "Action text cannot be null").trim();
        this.urgency = urgency != null ? urgency : "ROUTINE";
        this.isNegativeAction = isNegativeAction;
    }

    public static RecommendedAction of(int order, String text, String urgency) {
        return new RecommendedAction(UUID.randomUUID(), order, text, urgency, false);
    }

    public static RecommendedAction doNot(int order, String text) {
        return new RecommendedAction(UUID.randomUUID(), order, text, "IMMEDIATE", true);
    }
}
