package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.InsightCategory;
import com.aquaai.analytics.domain.model.InsightSeverity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class FarmInsightDto {
    private final String id;
    private final InsightCategory category;
    private final InsightSeverity severity;
    private final UUID fieldId;
    private final String fieldName;
    private final String title;
    private final String explanation;
    private final List<String> evidence;
    private final String recommendedAction;
    private final String targetSection;
    private final Map<String, String> targetPayload;
    private final Instant createdAt;

    @JsonProperty("what")
    public String getWhat() {
        return title;
    }

    @JsonProperty("why")
    public String getWhy() {
        return explanation;
    }

    @JsonProperty("nextStep")
    public String getNextStep() {
        return recommendedAction;
    }
}
