package com.aquaai.accountability.domain.model;

/**
 * Deterministic severity classification for operational water incidents.
 * Hydraulic safety incidents retain absolute operational priority.
 * Severity represents operational/physical impact and is independent of evidence confidence.
 */
public enum IncidentSeverity {
    CRITICAL(4),       // 4-hour target investigation / immediate review
    HIGH(24),          // 24-hour review target
    MEDIUM(72),        // 72-hour review target
    LOW(168),          // 7-day review target (168 hours)
    INFORMATIONAL(0);  // No mandatory investigation deadline

    private final int defaultSlaHours;

    IncidentSeverity(int defaultSlaHours) {
        this.defaultSlaHours = defaultSlaHours;
    }

    public int getDefaultSlaHours() {
        return defaultSlaHours;
    }

    public boolean hasMandatoryDeadline() {
        return defaultSlaHours > 0;
    }
}
