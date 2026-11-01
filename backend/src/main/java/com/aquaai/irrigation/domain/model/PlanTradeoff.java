package com.aquaai.irrigation.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing an operational trade-off or decision explanation factor.
 */
@Getter
@Builder
public class PlanTradeoff {

    private final UUID id;
    private final String category;
    private final String description;

    public PlanTradeoff(UUID id, String category, String description) {
        this.id = id != null ? id : UUID.randomUUID();
        this.category = Objects.requireNonNull(category, "Tradeoff category must not be null");
        this.description = Objects.requireNonNull(description, "Tradeoff description must not be null");
    }
}
