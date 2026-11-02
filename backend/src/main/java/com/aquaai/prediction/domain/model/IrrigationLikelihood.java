package com.aquaai.prediction.domain.model;

/**
 * Likelihood that a field will require irrigation within the designated forecast horizon.
 */
public enum IrrigationLikelihood {
    NOT_EXPECTED,
    LOW_LIKELIHOOD,
    POSSIBLE,
    LIKELY,
    HIGHLY_LIKELY,
    UNKNOWN
}
