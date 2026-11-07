package com.aquaai.network.domain.model;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * Multi-parcel or infrastructure contention conflict detected during scheduling.
 */
@Builder
public record DispatchConflict(
        UUID id,
        String conflictType, // CAPACITY_EXCEEDED, SCHEDULE_OVERLAP, SOURCE_DEFICIT, SAFETY_INTERLOCK_BLOCKED, PUMP_UNAVAILABLE
        String severity,     // CRITICAL, HIGH, MEDIUM
        UUID canalSegmentId,
        String canalName,
        List<UUID> conflictingFieldIds,
        List<String> conflictingFieldNames,
        String message,
        String mitigationSuggestion
) {
}
