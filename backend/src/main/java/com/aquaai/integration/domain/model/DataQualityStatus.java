package com.aquaai.integration.domain.model;

/**
 * Data quality state classified by the multi-stage ingestion pipeline.
 */
public enum DataQualityStatus {
    VALID,
    INVALID,
    STALE,
    DUPLICATE,
    INCOMPLETE,
    OUT_OF_RANGE,
    UNVERIFIED;

    public boolean isAcceptable() {
        return this == VALID || this == UNVERIFIED;
    }
}
