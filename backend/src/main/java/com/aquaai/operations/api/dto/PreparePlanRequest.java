package com.aquaai.operations.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload to prepare or derive a smart irrigation plan for an operational parcel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreparePlanRequest {
    private LocalDate targetDate;
    private BigDecimal customVolumeM3;
    private String customNotes;
}
