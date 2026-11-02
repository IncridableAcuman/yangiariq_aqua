package com.aquaai.analytics.api.dto;

import com.aquaai.analytics.domain.model.DataConfidenceLevel;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class VerificationPerformanceDto {
    private final long completedOperationsCount;
    private final long verifiedOperationsCount;
    private final long unverifiedOperationsCount;
    private final BigDecimal verificationCoveragePercent;
    private final long successfulRechargeCount;
    private final long deficitRechargeCount;
    private final DataConfidenceLevel dataConfidence;
    private final String coverageNotice;

    @JsonProperty("totalOperationsToVerify")
    public long getTotalOperationsToVerify() {
        return completedOperationsCount;
    }

    @JsonProperty("unverifiedCompletedOperationsCount")
    public long getUnverifiedCompletedOperationsCount() {
        return unverifiedOperationsCount;
    }

    @JsonProperty("automatedVerificationPercent")
    public BigDecimal getAutomatedVerificationPercent() {
        return verificationCoveragePercent;
    }
}
