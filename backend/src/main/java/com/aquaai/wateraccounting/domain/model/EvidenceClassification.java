package com.aquaai.wateraccounting.domain.model;

/**
 * Explicit classification of data trustworthiness in water accounting.
 * Never present estimated or simulated water quantities as actual measurements.
 */
public enum EvidenceClassification {
    OBSERVED,
    CALCULATED,
    ESTIMATED,
    SIMULATED,
    MISSING
}
