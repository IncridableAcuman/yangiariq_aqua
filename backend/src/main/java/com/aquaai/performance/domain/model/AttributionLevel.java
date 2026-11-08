package com.aquaai.performance.domain.model;

/**
 * Conservative operational impact attribution classifications.
 * Never treats association as causation.
 */
public enum AttributionLevel {
    /**
     * Clear causal relationship supported by reliable, relevant evidence and an appropriate verification method.
     */
    DIRECTLY_VERIFIED,

    /**
     * Meaningful temporal and operational relationship exists, but confounding variables cannot be completely ruled out.
     */
    STRONGLY_ASSOCIATED,

    /**
     * Outcome changed during the period, but direct attribution is not established.
     */
    ASSOCIATED,

    /**
     * Conflicting evidence or insufficient observations.
     */
    INCONCLUSIVE,

    /**
     * Required baseline or post-intervention data is missing.
     */
    NOT_ASSESSABLE
}
