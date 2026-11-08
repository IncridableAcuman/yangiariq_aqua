package com.aquaai.performance.domain.model;

/**
 * Quality tiers for baseline datasets.
 * Invariant: An ESTIMATED or SIMULATED baseline can NEVER produce a verified real-world impact claim.
 */
public enum BaselineQuality {
    VERIFIED_BASELINE,
    PARTIALLY_VERIFIED_BASELINE,
    ESTIMATED_BASELINE,
    SIMULATED_BASELINE,
    INSUFFICIENT_BASELINE
}
