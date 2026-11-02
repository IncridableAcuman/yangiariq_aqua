package com.aquaai.prediction.domain.model;

/**
 * Operational lifecycle status of a predictive method or rule version.
 */
public enum MethodVersionStatus {
    DRAFT,
    CANDIDATE,
    APPROVED,
    SHADOW,
    CANARY,
    ACTIVE,
    DEPRECATED,
    RETIRED
}
