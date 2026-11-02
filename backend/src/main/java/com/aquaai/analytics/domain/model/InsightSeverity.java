package com.aquaai.analytics.domain.model;

/**
 * Severity rankings for actionable insights.
 * Safety and critical issues always supersede informational notes.
 */
public enum InsightSeverity {
    CRITICAL,
    HIGH,
    WARNING,
    INFO
}
