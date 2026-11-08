package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewIncidentRequest(
        @NotNull Boolean accept,
        @NotBlank @Size(max = 2000) String notes
) {}
