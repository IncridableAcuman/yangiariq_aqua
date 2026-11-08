package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record ProposeActionRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 1000) String objective,
        UUID actionOwnerId,
        @Size(max = 255) String actionOwnerName,
        String priority,
        Instant dueDate,
        @NotBlank @Size(max = 1000) String expectedOutcome,
        @Size(max = 1000) String verificationRequirement
) {}
