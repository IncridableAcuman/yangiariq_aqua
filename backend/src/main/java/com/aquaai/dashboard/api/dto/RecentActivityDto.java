package com.aquaai.dashboard.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class RecentActivityDto {
    private final String id;
    private final Instant timestamp;
    private final String activityType;
    private final String title;
    private final String description;
    private final String severity;
    private final UUID fieldId;
    private final String fieldName;
}
