package com.aquaai.recommendation.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;
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
 * Agronomic Recommendation Aggregate Root representing an actionable,
 * evidence-based decision support proposal for an agricultural field parcel.
 * <p>
 * Invariants & Lifecycle:
 * <ul>
 *   <li>Initial status is GENERATED.</li>
 *   <li>GENERATED -> VIEWED, ACCEPTED, DISMISSED, EXPIRED.</li>
 *   <li>VIEWED -> ACCEPTED, DISMISSED, EXPIRED.</li>
 *   <li>ACCEPTED -> COMPLETED, DISMISSED, EXPIRED.</li>
 *   <li>DISMISSED, COMPLETED, and EXPIRED are immutable terminal states.</li>
 *   <li>Dismissal requires a non-blank reason.</li>
 *   <li>Confidence is bounded within [0.10, 0.95].</li>
 * </ul>
 */
@Getter
public class AgronomicRecommendation {

    private final UUID id;
    private final UUID fieldId;
    private final RecommendationType recommendationType;
    private RecommendationPriority priority;
    private RecommendationStatus status;
    private String title;
    private String summary;
    private String explanation;
    private BigDecimal confidence;
    private ConfidenceLevel confidenceLevel;
    private final Instant validFrom;
    private Instant validUntil;
    private final Instant generatedAt;
    private final RecommendationSource source;
    private String limitations;
    private final boolean isSimulated;

    private Instant viewedAt;
    private UUID viewedBy;
    private Instant acceptedAt;
    private UUID acceptedBy;
    private Instant dismissedAt;
    private UUID dismissedBy;
    private String dismissalReason;
    private Instant completedAt;
    private UUID completedBy;

    private final List<RecommendationEvidence> evidence = new ArrayList<>();
    private final List<RecommendedAction> actions = new ArrayList<>();

    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    private AgronomicRecommendation(
            UUID id,
            UUID fieldId,
            RecommendationType recommendationType,
            RecommendationPriority priority,
            RecommendationStatus status,
            String title,
            String summary,
            String explanation,
            BigDecimal confidence,
            ConfidenceLevel confidenceLevel,
            Instant validFrom,
            Instant validUntil,
            Instant generatedAt,
            RecommendationSource source,
            String limitations,
            boolean isSimulated,
            Instant viewedAt,
            UUID viewedBy,
            Instant acceptedAt,
            UUID acceptedBy,
            Instant dismissedAt,
            UUID dismissedBy,
            String dismissalReason,
            Instant completedAt,
            UUID completedBy,
            List<RecommendationEvidence> evidence,
            List<RecommendedAction> actions,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.fieldId = Objects.requireNonNull(fieldId, "FieldId cannot be null");
        this.recommendationType = Objects.requireNonNull(recommendationType, "RecommendationType cannot be null");
        this.priority = Objects.requireNonNull(priority, "Priority cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.title = Objects.requireNonNull(title, "Title cannot be null");
        this.summary = Objects.requireNonNull(summary, "Summary cannot be null");
        this.explanation = Objects.requireNonNull(explanation, "Explanation cannot be null");
        this.confidence = clampConfidence(confidence);
        this.confidenceLevel = confidenceLevel != null ? confidenceLevel : deriveConfidenceLevel(this.confidence);
        this.validFrom = Objects.requireNonNull(validFrom, "ValidFrom cannot be null");
        this.validUntil = Objects.requireNonNull(validUntil, "ValidUntil cannot be null");
        this.generatedAt = Objects.requireNonNull(generatedAt, "GeneratedAt cannot be null");
        this.source = source != null ? source : RecommendationSource.HYBRID_AI;
        this.limitations = limitations;
        this.isSimulated = isSimulated;

        this.viewedAt = viewedAt;
        this.viewedBy = viewedBy;
        this.acceptedAt = acceptedAt;
        this.acceptedBy = acceptedBy;
        this.dismissedAt = dismissedAt;
        this.dismissedBy = dismissedBy;
        this.dismissalReason = dismissalReason;
        this.completedAt = completedAt;
        this.completedBy = completedBy;

        if (evidence != null) {
            this.evidence.addAll(evidence);
        }
        if (actions != null) {
            this.actions.addAll(actions);
        }

        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version != null ? version : 0L;
    }

    public static AgronomicRecommendation create(
            UUID fieldId,
            RecommendationType recommendationType,
            RecommendationPriority priority,
            String title,
            String summary,
            String explanation,
            BigDecimal confidence,
            Instant validFrom,
            Instant validUntil,
            RecommendationSource source,
            String limitations,
            boolean isSimulated,
            List<RecommendationEvidence> evidence,
            List<RecommendedAction> actions
    ) {
        Instant now = Instant.now();
        BigDecimal clamped = clampConfidence(confidence);
        ConfidenceLevel level = deriveConfidenceLevel(clamped);

        return new AgronomicRecommendation(
                UUID.randomUUID(),
                fieldId,
                recommendationType,
                priority,
                RecommendationStatus.GENERATED,
                title,
                summary,
                explanation,
                clamped,
                level,
                validFrom != null ? validFrom : now,
                validUntil != null ? validUntil : now.plusSeconds(86400),
                now,
                source,
                limitations,
                isSimulated,
                null, null,
                null, null,
                null, null, null,
                null, null,
                evidence,
                actions,
                now,
                now,
                0L
        );
    }

    public static AgronomicRecommendation reconstitute(
            UUID id,
            UUID fieldId,
            RecommendationType recommendationType,
            RecommendationPriority priority,
            RecommendationStatus status,
            String title,
            String summary,
            String explanation,
            BigDecimal confidence,
            ConfidenceLevel confidenceLevel,
            Instant validFrom,
            Instant validUntil,
            Instant generatedAt,
            RecommendationSource source,
            String limitations,
            boolean isSimulated,
            Instant viewedAt,
            UUID viewedBy,
            Instant acceptedAt,
            UUID acceptedBy,
            Instant dismissedAt,
            UUID dismissedBy,
            String dismissalReason,
            Instant completedAt,
            UUID completedBy,
            List<RecommendationEvidence> evidence,
            List<RecommendedAction> actions,
            Instant createdAt,
            Instant updatedAt,
            Long version
    ) {
        return new AgronomicRecommendation(
                id, fieldId, recommendationType, priority, status,
                title, summary, explanation, confidence, confidenceLevel,
                validFrom, validUntil, generatedAt, source, limitations, isSimulated,
                viewedAt, viewedBy, acceptedAt, acceptedBy, dismissedAt, dismissedBy, dismissalReason,
                completedAt, completedBy, evidence, actions, createdAt, updatedAt, version
        );
    }

    // -----------------------------------------------------------------
    // State Transitions
    // -----------------------------------------------------------------

    public void markViewed(UUID userId) {
        if (isTerminal()) {
            throw new BusinessRuleException(
                    "Cannot mark a terminal recommendation (" + status + ") as viewed.",
                    "INVALID_STATUS_TRANSITION");
        }
        if (this.status == RecommendationStatus.GENERATED) {
            this.status = RecommendationStatus.VIEWED;
        }
        if (this.viewedAt == null) {
            this.viewedAt = Instant.now();
            this.viewedBy = userId;
            this.updatedAt = this.viewedAt;
        }
    }

    public void accept(UUID userId) {
        assertNotTerminal("accept");
        this.status = RecommendationStatus.ACCEPTED;
        this.acceptedAt = Instant.now();
        this.acceptedBy = userId;
        this.updatedAt = this.acceptedAt;
    }

    public void dismiss(UUID userId, String reason) {
        assertNotTerminal("dismiss");
        if (reason == null || reason.trim().isBlank()) {
            throw new BusinessRuleException("Dismissal reason cannot be blank.", "VALIDATION_FAILED");
        }
        this.status = RecommendationStatus.DISMISSED;
        this.dismissedAt = Instant.now();
        this.dismissedBy = userId;
        this.dismissalReason = reason.trim();
        this.updatedAt = this.dismissedAt;
    }

    public void complete(UUID userId) {
        assertNotTerminal("complete");
        this.status = RecommendationStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.completedBy = userId;
        this.updatedAt = this.completedAt;
    }

    public void expire(Instant now) {
        if (isTerminal()) {
            return;
        }
        this.status = RecommendationStatus.EXPIRED;
        this.updatedAt = now != null ? now : Instant.now();
    }

    public boolean isExpired(Instant now) {
        if (now == null) {
            return false;
        }
        return now.isAfter(this.validUntil);
    }

    public boolean isTerminal() {
        return status == RecommendationStatus.DISMISSED
                || status == RecommendationStatus.COMPLETED
                || status == RecommendationStatus.EXPIRED;
    }

    public List<RecommendationEvidence> getEvidence() {
        return Collections.unmodifiableList(evidence);
    }

    public List<RecommendedAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

    private void assertNotTerminal(String action) {
        if (isTerminal()) {
            throw new BusinessRuleException(
                    "Cannot " + action + " a recommendation in terminal status: " + status,
                    "INVALID_STATUS_TRANSITION");
        }
    }

    private static BigDecimal clampConfidence(BigDecimal conf) {
        if (conf == null) {
            return BigDecimal.valueOf(0.50);
        }
        BigDecimal scaled = conf.setScale(4, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.valueOf(0.10)) < 0) {
            return BigDecimal.valueOf(0.10);
        }
        if (scaled.compareTo(BigDecimal.valueOf(0.95)) > 0) {
            return BigDecimal.valueOf(0.95);
        }
        return scaled;
    }

    private static ConfidenceLevel deriveConfidenceLevel(BigDecimal conf) {
        if (conf.compareTo(BigDecimal.valueOf(0.80)) >= 0) {
            return ConfidenceLevel.HIGH;
        }
        if (conf.compareTo(BigDecimal.valueOf(0.60)) >= 0) {
            return ConfidenceLevel.MODERATE;
        }
        return ConfidenceLevel.LOW;
    }
}
