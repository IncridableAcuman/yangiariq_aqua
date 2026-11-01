package com.aquaai.irrigation.api.dto;

import com.aquaai.irrigation.domain.model.PlanTradeoff;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PlanTradeoffResponse {

    private final UUID id;
    private final String category;
    private final String description;

    public static PlanTradeoffResponse fromDomain(PlanTradeoff t) {
        if (t == null) {
            return null;
        }
        return PlanTradeoffResponse.builder()
                .id(t.getId())
                .category(t.getCategory())
                .description(t.getDescription())
                .build();
    }
}
