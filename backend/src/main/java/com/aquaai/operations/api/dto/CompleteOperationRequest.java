package com.aquaai.operations.api.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload to record manual field operation completion and actual delivered volume.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteOperationRequest {

    @DecimalMin(value = "0.0", inclusive = true, message = "Actual delivered volume cannot be negative.")
    private BigDecimal actualVolumeDeliveredM3;

    private Instant endedAt;
    private String operatorNotes;
}
