package com.aquaai.water.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Temporal query window boundary representation in UTC")
public record DateRangeDto(
        @Schema(description = "Window start timestamp (inclusive) in ISO-8601 UTC", example = "2026-04-01T00:00:00Z")
        Instant from,

        @Schema(description = "Window end timestamp (inclusive) in ISO-8601 UTC", example = "2026-09-30T23:59:59Z")
        Instant to
) {
}
