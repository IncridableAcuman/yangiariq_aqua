package com.aquaai.prediction.domain.model;

/**
 * Categorical degree of matching between operational prediction context and historical evidence.
 */
public enum ContextMatchLevel {
    EXACT_CONTEXT,     // Crop, soil, irrigation method, and telemetry quality match historical validated evidence
    PARTIAL_CONTEXT,   // Core agronomic dimensions (crop + soil or crop + irrigation) match
    SIMILAR_CONTEXT,   // Similar crop class or comparable soil characteristics
    BASELINE_FALLBACK, // Insufficient context-specific evidence; active production baseline selected
    INSUFFICIENT_DATA  // Insufficient telemetry or unconfigured parcel metadata
}
