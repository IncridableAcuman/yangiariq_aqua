package com.aquaai.irrigation.api.dto;

import com.aquaai.irrigation.domain.model.ConstraintStatus;
import com.aquaai.irrigation.domain.model.ConstraintType;
import com.aquaai.irrigation.domain.model.PlanConstraint;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PlanConstraintResponse {

    private final UUID id;
    private final ConstraintType type;
    private final ConstraintStatus status;
    private final String message;
    private final String details;

    public static PlanConstraintResponse fromDomain(PlanConstraint c) {
        if (c == null) {
            return null;
        }
        return PlanConstraintResponse.builder()
                .id(c.getId())
                .type(c.getType())
                .status(c.getStatus())
                .message(c.getMessage())
                .details(c.getDetails())
                .build();
    }
}
