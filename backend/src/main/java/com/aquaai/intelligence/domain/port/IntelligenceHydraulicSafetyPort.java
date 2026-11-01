package com.aquaai.intelligence.domain.port;

import java.util.UUID;

/**
 * Domain port for evaluating hydraulic safety interlock constraints.
 */
public interface IntelligenceHydraulicSafetyPort {

    record HydraulicSafetyStatus(
            boolean isBlocked,
            String blockedReason
    ) {}

    HydraulicSafetyStatus checkHydraulicSafety(UUID fieldId);
}
