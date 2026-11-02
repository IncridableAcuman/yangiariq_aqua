package com.aquaai.operations.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Payload to record manual field operation commencement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartOperationRequest {
    private Instant startedAt;
    private String operatorNotes;
}
