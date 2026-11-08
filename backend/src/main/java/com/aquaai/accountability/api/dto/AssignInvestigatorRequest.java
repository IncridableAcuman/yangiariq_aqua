package com.aquaai.accountability.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AssignInvestigatorRequest(
        @NotNull UUID investigatorId,
        @Size(max = 255) String investigatorName,
        @Size(max = 255) String organization,
        @Size(max = 2000) String reason
) {}
