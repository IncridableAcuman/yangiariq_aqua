package com.aquaai.anomaly.api.dto;

import jakarta.validation.constraints.Size;

public record ActionAnomalyRequest(
        @Size(max = 1000)
        String notes
) {
}
