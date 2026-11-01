package com.aquaai.recommendation.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recommendation_actions")
@Getter
@Setter
@NoArgsConstructor
public class RecommendedActionJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "recommendation_id", nullable = false)
    private UUID recommendationId;

    @Column(name = "action_order", nullable = false)
    private int actionOrder;

    @Column(name = "action_text", nullable = false, length = 500)
    private String actionText;

    @Column(name = "urgency", nullable = false, length = 32)
    private String urgency;

    @Column(name = "is_negative_action", nullable = false)
    private boolean negativeAction;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RecommendedActionJpaEntity(
            UUID id,
            UUID recommendationId,
            int actionOrder,
            String actionText,
            String urgency,
            boolean negativeAction,
            Instant createdAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.recommendationId = recommendationId;
        this.actionOrder = actionOrder;
        this.actionText = actionText;
        this.urgency = urgency != null ? urgency : "ROUTINE";
        this.negativeAction = negativeAction;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }
}
