package com.aquaai.operations.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Details of operational schedule conflicts (overlapping plans, water shortages, or hydraulic locks).
 */
@Value
@Builder
public class ScheduleConflictDto {
    boolean hasConflict;
    String conflictType; // TIME_OVERLAP, INSUFFICIENT_WATER, HYDRAULIC_BLOCK, FIELD_BUSY
    String conflictMessage;
    UUID conflictingPlanId;
    Instant conflictingStartTime;
    Instant conflictingEndTime;
    String suggestedAdjustment;
}
