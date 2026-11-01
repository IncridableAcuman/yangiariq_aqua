package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class UrgentActionDto {
    private final String id;
    private final String actionType;
    private final String severity;
    private final String title;
    private final String description;
    private final UUID fieldId;
    private final String fieldName;
    private final String targetSection;
    private final String actionLabel;
    private final Map<String, String> actionPayload;
    private final Instant createdAt;
}
