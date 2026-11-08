package com.aquaai.accountability.domain.service;

import java.util.Objects;
import java.util.UUID;

/**
 * Pure domain service for calculating idempotent deduplication keys for operational findings.
 */
public class IncidentDeduplicationEngine {

    public String buildDeduplicationKey(String sourceModule, String sourceEntityType, UUID sourceEntityId) {
        Objects.requireNonNull(sourceModule, "Source module cannot be null");
        Objects.requireNonNull(sourceEntityType, "Source entity type cannot be null");
        return (sourceModule.trim().toUpperCase() + ":" +
                sourceEntityType.trim().toUpperCase() + ":" +
                (sourceEntityId != null ? sourceEntityId.toString() : "GLOBAL")).toLowerCase();
    }
}
