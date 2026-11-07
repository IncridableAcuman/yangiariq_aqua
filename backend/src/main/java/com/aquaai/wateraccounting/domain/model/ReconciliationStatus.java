package com.aquaai.wateraccounting.domain.model;

public enum ReconciliationStatus {
    RECONCILED,
    WITHIN_TOLERANCE,
    DISCREPANCY,
    UNVERIFIED,
    INSUFFICIENT_DATA
}
