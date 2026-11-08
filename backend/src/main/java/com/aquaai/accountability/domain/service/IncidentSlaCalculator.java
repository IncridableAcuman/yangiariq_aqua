package com.aquaai.accountability.domain.service;

import com.aquaai.accountability.domain.model.IncidentSeverity;
import com.aquaai.accountability.domain.model.IncidentStatus;

import java.time.Duration;
import java.time.Instant;

/**
 * Pure domain service for calculating deterministic SLA deadlines and detecting overdue breaches.
 */
public class IncidentSlaCalculator {

    public Instant calculateDeadline(Instant detectionTime, IncidentSeverity severity) {
        if (detectionTime == null || severity == null) {
            return null;
        }
        int slaHours = severity.getDefaultSlaHours();
        if (slaHours <= 0) {
            return null; // Informational has no mandatory deadline
        }
        return detectionTime.plus(Duration.ofHours(slaHours));
    }

    public boolean isOverdue(Instant deadline, IncidentStatus status, Instant referenceNow) {
        if (deadline == null || status == null || referenceNow == null) {
            return false;
        }
        if (status.isTerminal()) {
            return false;
        }
        return referenceNow.isAfter(deadline);
    }

    public long calculateRemainingHours(Instant deadline, Instant referenceNow) {
        if (deadline == null || referenceNow == null) {
            return 0;
        }
        return Duration.between(referenceNow, deadline).toHours();
    }
}
