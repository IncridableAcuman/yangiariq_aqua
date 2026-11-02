package com.aquaai.prediction.domain.model;

/**
 * Status of forecast accuracy evaluation once the target horizon has transpired.
 */
public enum PredictionAccuracyStatus {
    PENDING,
    VERIFIED_ACCURATE,
    VERIFIED_INACCURATE,
    EXPIRED,
    UNVERIFIABLE
}
