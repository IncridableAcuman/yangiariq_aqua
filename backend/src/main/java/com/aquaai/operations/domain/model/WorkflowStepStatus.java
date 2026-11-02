package com.aquaai.operations.domain.model;

/**
 * Status of an individual operational timeline milestone step.
 */
public enum WorkflowStepStatus {
    COMPLETED,
    CURRENT,
    UPCOMING,
    BLOCKED,
    SKIPPED,
    REJECTED
}
