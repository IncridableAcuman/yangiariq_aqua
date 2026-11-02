package com.aquaai.operations.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rejection request payload requiring an explanatory reason.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectOperationalPlanRequest {

    @NotBlank(message = "Rejection reason must not be blank.")
    @Size(min = 3, max = 500, message = "Rejection reason must be between 3 and 500 characters.")
    private String reason;
}
