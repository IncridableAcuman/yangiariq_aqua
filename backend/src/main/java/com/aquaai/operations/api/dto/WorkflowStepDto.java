package com.aquaai.operations.api.dto;

import com.aquaai.operations.domain.model.WorkflowStepStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Discrete milestone step in the end-to-end operational workflow timeline.
 */
@Value
@Builder
public class WorkflowStepDto {
    String stepKey;
    String title;
    WorkflowStepStatus status;
    Instant timestamp;
    String actor;
    String reason;
    String availableAction;
}
