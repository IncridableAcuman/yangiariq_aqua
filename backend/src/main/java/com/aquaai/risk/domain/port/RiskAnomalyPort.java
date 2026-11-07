package com.aquaai.risk.domain.port;

import java.util.UUID;

public interface RiskAnomalyPort {

    boolean hasSuspectedPipeBurst(UUID fieldId);

    boolean hasCriticalPressureDrop(UUID fieldId);

    int countActiveAnomalies(UUID fieldId);
}
