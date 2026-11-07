package com.aquaai.network.domain.port;

import java.util.UUID;

public interface NetworkAnomalyPort {
    boolean hasSuspectedPipeBurst(UUID fieldId);
    boolean hasCriticalPressureDrop(UUID fieldId);
}
