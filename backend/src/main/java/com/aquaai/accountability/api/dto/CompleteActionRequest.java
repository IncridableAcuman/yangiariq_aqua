package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CompleteActionRequest(
        UUID actionId,
        @NotBlank @Size(max = 2000) String completionNotes
) {}
