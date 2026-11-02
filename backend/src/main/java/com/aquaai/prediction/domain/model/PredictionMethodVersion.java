package com.aquaai.prediction.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable versioned representation of a predictive method, formula, or soft rule configuration.
 * <p>
 * Ensures reproducibility: historical predictions remain forever linked to the version
 * that generated them. Once active, its configuration cannot be mutated in place.
 */
public class PredictionMethodVersion {

    private final UUID id;
    private final PredictionType predictionType;
    private final String versionTag;
    private MethodVersionStatus status;
    private final String description;
    private String configurationJson;
    private boolean isImmutable;
    private final Instant createdAt;
    private Instant activatedAt;
    private Instant retiredAt;
    private final String createdBy;
    private String activatedBy;
    private Long optLockVersion;

    public PredictionMethodVersion(
            UUID id,
            PredictionType predictionType,
            String versionTag,
            MethodVersionStatus status,
            String description,
            String configurationJson,
            boolean isImmutable,
            Instant createdAt,
            Instant activatedAt,
            Instant retiredAt,
            String createdBy,
            String activatedBy,
            Long optLockVersion
    ) {
        this.id = id != null ? id : UUID.randomUUID();
        this.predictionType = Objects.requireNonNull(predictionType, "predictionType must not be null");
        this.versionTag = Objects.requireNonNull(versionTag, "versionTag must not be null");
        this.status = status != null ? status : MethodVersionStatus.DRAFT;
        this.description = description != null ? description : "";
        this.configurationJson = Objects.requireNonNull(configurationJson, "configurationJson must not be null");
        this.isImmutable = isImmutable;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.activatedAt = activatedAt;
        this.retiredAt = retiredAt;
        this.createdBy = createdBy != null ? createdBy : "SYSTEM";
        this.activatedBy = activatedBy;
        this.optLockVersion = optLockVersion != null ? optLockVersion : 0L;

        validateSafetyParameters(this.configurationJson);
    }

    public static PredictionMethodVersion createCandidate(
            PredictionType predictionType,
            String versionTag,
            String description,
            String configurationJson,
            String createdBy
    ) {
        return new PredictionMethodVersion(
                UUID.randomUUID(),
                predictionType,
                versionTag,
                MethodVersionStatus.CANDIDATE,
                description,
                configurationJson,
                false,
                Instant.now(),
                null,
                null,
                createdBy,
                null,
                0L
        );
    }

    /**
     * Hard safety check: Predictive governance MUST NEVER attempt to modify hydraulic safety thresholds,
     * emergency interlock cutoffs, or pump trip constraints.
     */
    private static void validateSafetyParameters(String configurationJson) {
        if (configurationJson == null) return;
        String lower = configurationJson.toLowerCase();
        if (lower.contains("safetyinterlock") || lower.contains("hydraulicsafety") || lower.contains("bypass_safety")
                || lower.contains("pipeburst") || lower.contains("emergencycutoff")) {
            throw new IllegalArgumentException("Hard hydraulic safety parameters cannot be modified by predictive governance.");
        }
    }

    public void updateConfiguration(String newConfig) {
        if (this.isImmutable) {
            throw new IllegalStateException("Cannot modify immutable prediction method version: " + versionTag);
        }
        validateSafetyParameters(newConfig);
        this.configurationJson = newConfig;
    }

    public void markImmutable() {
        this.isImmutable = true;
    }

    public void activate(String actor) {
        if (this.status == MethodVersionStatus.ACTIVE) {
            return; // Idempotent
        }
        this.status = MethodVersionStatus.ACTIVE;
        this.activatedAt = Instant.now();
        this.activatedBy = actor != null ? actor : "SYSTEM";
        this.isImmutable = true;
    }

    public void approve(String actor) {
        this.status = MethodVersionStatus.APPROVED;
    }

    public void setShadow(String actor) {
        this.status = MethodVersionStatus.SHADOW;
        this.isImmutable = true;
    }

    public void setCanary(String actor) {
        this.status = MethodVersionStatus.CANARY;
        this.isImmutable = true;
    }

    public void deprecate(String actor) {
        this.status = MethodVersionStatus.DEPRECATED;
        this.retiredAt = Instant.now();
        this.isImmutable = true;
    }

    public void deprecate() {
        deprecate("SYSTEM");
    }

    public void retire(String actor) {
        this.status = MethodVersionStatus.RETIRED;
        this.retiredAt = Instant.now();
        this.isImmutable = true;
    }

    // Getters
    public UUID getId() { return id; }
    public PredictionType getPredictionType() { return predictionType; }
    public String getVersionTag() { return versionTag; }
    public MethodVersionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getConfigurationJson() { return configurationJson; }
    public boolean isImmutable() { return isImmutable; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getActivatedAt() { return activatedAt; }
    public Instant getRetiredAt() { return retiredAt; }
    public String getCreatedBy() { return createdBy; }
    public String getActivatedBy() { return activatedBy; }
    public Long getOptLockVersion() { return optLockVersion; }
}
