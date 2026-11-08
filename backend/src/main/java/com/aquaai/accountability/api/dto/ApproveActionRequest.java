package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ApproveActionRequest(
        UUID actionId,
        @Size(max = 1000) String notes
) {}
