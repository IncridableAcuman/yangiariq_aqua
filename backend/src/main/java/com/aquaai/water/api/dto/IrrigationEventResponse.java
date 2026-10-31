package com.aquaai.water.api.dto;

import com.aquaai.water.domain.model.IrrigationEvent;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Executed parcel irrigation event representation")
public record IrrigationEventResponse(
        @Schema(description = "Unique immutable identifier", example = "e1f2a3b4-c5d6-7e8f-9a0b-1c2d3e4f5a6b")
        UUID id,

        @Schema(description = "Associated field parcel identifier", example = "b7a2d4b3-c157-4f67-8854-94c34a2e5d71")
        UUID fieldId,

        @Schema(description = "Optional parent scheduled irrigation plan ID", example = "f1e2d3c4-b5a6-7890-1234-56789abcdef0")
        UUID irrigationPlanId,

        @Schema(description = "Execution start timestamp in ISO-8601 UTC")
        Instant startedAt,

        @Schema(description = "Execution end timestamp in ISO-8601 UTC")
        Instant endedAt,

        @Schema(description = "Actual water volume consumed in cubic meters (m³)", example = "430.5000")
        BigDecimal actualVolumeM3,

        @Schema(description = "Event operational status", example = "COMPLETED")
        IrrigationEventStatus status,

        @Schema(description = "Operational remarks or meter logs", example = "Watering completed with uniform furrows")
        String notes,

        @Schema(description = "Creation timestamp")
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        Instant updatedAt
) {
    public static IrrigationEventResponse from(IrrigationEvent domain) {
        if (domain == null) {
            return null;
        }
        return new IrrigationEventResponse(
                domain.getId(),
                domain.getFieldId(),
                domain.getIrrigationPlanId(),
                domain.getStartedAt(),
                domain.getEndedAt(),
                domain.getActualVolumeM3(),
                domain.getStatus(),
                domain.getNotes(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
