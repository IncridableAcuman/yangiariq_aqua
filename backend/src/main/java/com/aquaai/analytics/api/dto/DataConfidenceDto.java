package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.DataConfidenceLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataConfidenceDto {
    private final DataConfidenceLevel overall;
    private final DataConfidenceLevel waterConfidence;
    private final DataConfidenceLevel telemetryConfidence;
    private final DataConfidenceLevel verificationConfidence;
    private final boolean hasIncompleteData;
    private final String warningMessage;
}
