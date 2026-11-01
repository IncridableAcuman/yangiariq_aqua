package com.aquaai.sensor.domain.model;

import com.aquaai.shared.domain.BusinessRuleException;

/**
 * Operational lifecycle states for field telemetry sensors.
 * <p>
 * Supported transitions:
 * <ul>
 *   <li>ACTIVE &rarr; INACTIVE, MAINTENANCE, OFFLINE</li>
 *   <li>INACTIVE &rarr; ACTIVE</li>
 *   <li>MAINTENANCE &rarr; ACTIVE, INACTIVE</li>
 *   <li>OFFLINE &rarr; ACTIVE, MAINTENANCE, INACTIVE</li>
 * </ul>
 */
public enum SensorStatus {
    ACTIVE,
    INACTIVE,
    MAINTENANCE,
    OFFLINE;

    public boolean canTransitionTo(SensorStatus target) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case ACTIVE -> target == INACTIVE || target == MAINTENANCE || target == OFFLINE;
            case INACTIVE -> target == ACTIVE;
            case MAINTENANCE -> target == ACTIVE || target == INACTIVE;
            case OFFLINE -> target == ACTIVE || target == MAINTENANCE || target == INACTIVE;
        };
    }

    public void validateTransition(SensorStatus target) {
        if (!canTransitionTo(target)) {
            throw new BusinessRuleException(
                    String.format("Invalid sensor status transition from %s to %s", this, target),
                    "INVALID_STATUS_TRANSITION"
            );
        }
    }
}
