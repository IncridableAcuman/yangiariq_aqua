package com.aquaai.operations.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Payload to finalize or adjust scheduled dispatch window.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOperationalPlanRequest {
    private Instant scheduledStartAt;
    private Instant scheduledEndAt;
    private String dispatchNotes;
}
