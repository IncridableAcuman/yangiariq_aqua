package com.aquaai.prediction.application.dto;

import com.aquaai.prediction.domain.model.EvaluationConfidenceTier;
import com.aquaai.prediction.domain.model.EvaluationDataQualityImpact;
import com.aquaai.prediction.domain.model.EvaluationDriftSummary;
import com.aquaai.prediction.domain.model.EvaluationInsight;
import com.aquaai.prediction.domain.model.HumanDecisionImpact;
import com.aquaai.prediction.domain.model.ModelHealthStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record PredictionEvaluationOverviewResponse(
        String periodLabel,
        Instant from,
        Instant to,
        int totalPredictionsGenerated,
        int totalEvaluated,
        int pendingEvaluationsCount,
        int insufficientDataCount,
        int interruptedCount,
        BigDecimal evaluationCoveragePercent,
        ModelHealthStatus overallModelHealth,
        List<String> healthReasons,
        EvaluationDriftSummary driftSummary,
        List<PredictionTypePerformanceDto> performanceByType,
        List<EvaluationConfidenceTier> confidenceCalibration,
        List<EvaluationDataQualityImpact> dataQualityImpact,
        List<EvaluationInsight> actionableInsights,
        List<HumanDecisionImpact> humanDecisionAudits
) {}
