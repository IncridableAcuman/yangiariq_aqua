package com.aquaai.performance.domain.model;

/**
 * Distinguishes official engineering limits or policy thresholds from operational and demo targets.
 * Never presents a demo target as an official government standard.
 */
public enum TargetCategory {
    POLICY_THRESHOLD,
    ENGINEERING_CONSTRAINT,
    OPERATIONAL_TARGET,
    PILOT_DEMO_TARGET
}
