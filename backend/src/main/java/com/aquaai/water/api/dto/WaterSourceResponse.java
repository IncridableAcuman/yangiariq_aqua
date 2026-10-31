package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.WaterSource;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Regional water distribution infrastructure representation")
public record WaterSourceResponse(
        @Schema(description = "Unique immutable identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        UUID id,

        @Schema(description = "Infrastructure name", example = "Palvon-Yobi Main Canal")
        String name,

        @Schema(description = "Hydraulic classification", example = "CANAL")
        WaterSourceType type,

        @Schema(description = "Operational description or landmarks", example = "Regional canal supplying western farms")
        String description,

        @Schema(description = "Operational status", example = "ACTIVE")
        WaterSourceStatus status,

        @Schema(description = "Registration timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static WaterSourceResponse from(WaterSource domain) {
        if (domain == null) {
            return null;
        }
        return new WaterSourceResponse(
                domain.getId(),
                domain.getName(),
                domain.getType(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
