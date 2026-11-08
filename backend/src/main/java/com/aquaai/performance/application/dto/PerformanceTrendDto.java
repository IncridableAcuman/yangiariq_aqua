package com.aquaai.performance.application.dto;

import com.aquaai.performance.domain.model.EvidenceClassification;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class PerformanceTrendDto {

    private final String indicatorCode;
    private final String indicatorName;
    private final String measurementUnit;
    private final List<DataPoint> dataPoints;

    @Getter
    @Builder
    public static class DataPoint {
        private final Instant timestamp;
        private final String label;
        private final BigDecimal actualValue;
        private final BigDecimal baselineValue;
        private final BigDecimal targetValue;
        private final EvidenceClassification evidenceClassification;
        private final boolean isMissing;
        private final boolean isSimulated;
    }
}
