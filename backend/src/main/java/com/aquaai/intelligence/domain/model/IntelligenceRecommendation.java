package com.aquaai.intelligence.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an advisory recommendation produced by the intelligence engine.
 * Strictly decision-support only; cannot actuate physical hardware.
 */
@Getter
@Builder
public class IntelligenceRecommendation {

    private final UUID id;
    private final UUID fieldId;
    private final String recommendationType;
    private final IntelligenceSeverity priority;
    private final String reason;
    private final BigDecimal recommendedVolumeM3;
    private final Integer recommendedDurationMinutes;
    private final Instant recommendedWindowStart;
    private final Instant recommendedWindowEnd;
    private final IntelligenceConfidence confidence;
    @Builder.Default
    private final List<String> constraints = new ArrayList<>();
    @Builder.Default
    private final List<AgronomicSignalType> supportingSignals = new ArrayList<>();
    private final String status;
    private final Instant generatedAt;

    public IntelligenceRecommendation(
            UUID id,
            UUID fieldId,
            String recommendationType,
            IntelligenceSeverity priority,
            String reason,
            BigDecimal recommendedVolumeM3,
            Integer recommendedDurationMinutes,
            Instant recommendedWindowStart,
            Instant recommendedWindowEnd,
            IntelligenceConfidence confidence,
            List<String> constraints,
            List<AgronomicSignalType> supportingSignals,
            String status,
            Instant generatedAt
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.fieldId = Objects.requireNonNull(fieldId, "fieldId must not be null");
        this.recommendationType = Objects.requireNonNull(recommendationType, "recommendationType must not be null");
        this.priority = priority != null ? priority : IntelligenceSeverity.INFO;
        this.reason = Objects.requireNonNull(reason, "reason must not be null");
        this.recommendedVolumeM3 = recommendedVolumeM3 != null ? recommendedVolumeM3.setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        this.recommendedDurationMinutes = recommendedDurationMinutes != null ? recommendedDurationMinutes : 0;
        this.recommendedWindowStart = recommendedWindowStart;
        this.recommendedWindowEnd = recommendedWindowEnd;
        this.confidence = confidence != null ? confidence : IntelligenceConfidence.MEDIUM;
        this.constraints = constraints != null ? new ArrayList<>(constraints) : new ArrayList<>();
        this.supportingSignals = supportingSignals != null ? new ArrayList<>(supportingSignals) : new ArrayList<>();
        this.status = status != null ? status : "NEW";
        this.generatedAt = generatedAt != null ? generatedAt : Instant.now();
    }

    public List<String> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public List<AgronomicSignalType> getSupportingSignals() {
        return Collections.unmodifiableList(supportingSignals);
    }
}
