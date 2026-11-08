package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReopenIncidentRequest(
        @NotBlank @Size(max = 2000) String reason
) {}
