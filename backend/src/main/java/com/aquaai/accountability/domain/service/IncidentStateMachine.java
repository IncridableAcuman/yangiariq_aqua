package com.aquaai.accountability.domain.service;

import com.aquaai.accountability.domain.model.IncidentStatus;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Pure domain state machine governing legal transitions in the incident investigation lifecycle.
 */
public class IncidentStateMachine {

    private static final Map<IncidentStatus, Set<IncidentStatus>> LEGAL_TRANSITIONS = new EnumMap<>(IncidentStatus.class);

    static {
        LEGAL_TRANSITIONS.put(IncidentStatus.DETECTED, EnumSet.of(
                IncidentStatus.UNDER_REVIEW,
                IncidentStatus.OPEN,
                IncidentStatus.DISMISSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.UNDER_REVIEW, EnumSet.of(
                IncidentStatus.OPEN,
                IncidentStatus.DISMISSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.OPEN, EnumSet.of(
                IncidentStatus.INVESTIGATING,
                IncidentStatus.ACTION_PROPOSED,
                IncidentStatus.DISMISSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.INVESTIGATING, EnumSet.of(
                IncidentStatus.ACTION_PROPOSED,
                IncidentStatus.ACTION_APPROVED,
                IncidentStatus.ACTION_IN_PROGRESS,
                IncidentStatus.DISMISSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.ACTION_PROPOSED, EnumSet.of(
                IncidentStatus.ACTION_APPROVED,
                IncidentStatus.INVESTIGATING,
                IncidentStatus.DISMISSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.ACTION_APPROVED, EnumSet.of(
                IncidentStatus.ACTION_IN_PROGRESS,
                IncidentStatus.INVESTIGATING
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.ACTION_IN_PROGRESS, EnumSet.of(
                IncidentStatus.PENDING_VERIFICATION,
                IncidentStatus.INVESTIGATING
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.PENDING_VERIFICATION, EnumSet.of(
                IncidentStatus.RESOLVED,
                IncidentStatus.INVESTIGATING,
                IncidentStatus.ACTION_IN_PROGRESS
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.RESOLVED, EnumSet.of(
                IncidentStatus.REOPENED,
                IncidentStatus.CLOSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.DISMISSED, EnumSet.of(
                IncidentStatus.REOPENED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.REOPENED, EnumSet.of(
                IncidentStatus.OPEN,
                IncidentStatus.INVESTIGATING,
                IncidentStatus.ACTION_PROPOSED
        ));

        LEGAL_TRANSITIONS.put(IncidentStatus.CLOSED, EnumSet.of(
                IncidentStatus.REOPENED
        ));
    }

    public boolean canTransition(IncidentStatus current, IncidentStatus target) {
        if (current == null || target == null) {
            return false;
        }
        if (current == target) {
            return true;
        }
        Set<IncidentStatus> allowed = LEGAL_TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }

    public void validateTransition(IncidentStatus current, IncidentStatus target, String reason) {
        if (!canTransition(current, target)) {
            throw new IllegalStateException("Illegal state transition from " + current + " to " + target);
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Transition reason must be provided for auditability");
        }
    }
}
