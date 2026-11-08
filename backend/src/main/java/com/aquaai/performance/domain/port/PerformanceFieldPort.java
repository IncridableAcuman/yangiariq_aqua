package com.aquaai.performance.domain.port;

import java.util.UUID;

public interface PerformanceFieldPort {
    boolean isFieldOwnedBy(UUID fieldId, UUID userId);
    String getFieldName(UUID fieldId);
}
