package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.BacktestRun;
import com.aquaai.prediction.domain.model.EvaluationDataQuality;
import com.aquaai.prediction.domain.model.EvaluationStatus;
import com.aquaai.prediction.domain.model.PredictionEvaluation;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.PredictionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BacktestingEngine Domain Service Unit Tests")
class BacktestingEngineUnitTest {

    private RegressionGuardService regressionGuardService;
    private BacktestingEngine backtestingEngine;

    @BeforeEach
    void setUp() {
        regressionGuardService = new RegressionGuardService();
        backtestingEngine = new BacktestingEngine(regressionGuardService);
    }

    @Test
    @DisplayName("Should handle empty historical evaluations gracefully")
    void shouldHandleEmptyEvaluations() {
        UUID proposalId = UUID.randomUUID();
        BacktestRun run = backtestingEngine.executeBacktest(
                proposalId,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                PredictionType.SOIL_MOISTURE_FORECAST,
                "{\"smoothingAlpha\":0.25}",
                Collections.emptyList(),
                Instant.now().minus(30, ChronoUnit.DAYS),
                Instant.now(),
                "TEST_OPERATOR"
        );

        assertThat(run).isNotNull();
        assertThat(run.getProposalId()).isEqualTo(proposalId);
        assertThat(run.getSampleSize()).isEqualTo(0);
        assertThat(run.isComparable()).isFalse();
        assertThat(run.isHasSufficientSample()).isFalse();
        assertThat(run.getDataQuality()).isEqualTo(EvaluationDataQuality.DATA_INSUFFICIENT);
    }

    @Test
    @DisplayName("Should evaluate historical evaluations and compute improvement accurately")
    void shouldExecuteBacktestWithImprovements() {
        UUID proposalId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        Instant now = Instant.now();

        List<PredictionEvaluation> evaluations = new ArrayList<>();
        // Generate 12 historical evaluations where actual is slightly higher than prediction
        for (int i = 0; i < 12; i++) {
            UUID predId = UUID.randomUUID();
            BigDecimal predicted = BigDecimal.valueOf(25.0);
            BigDecimal actual = BigDecimal.valueOf(30.0);

            evaluations.add(PredictionEvaluation.createNumerical(
                    predId,
                    fieldId,
                    PredictionType.SOIL_MOISTURE_FORECAST,
                    PredictionHorizon.NEXT_24_HOURS,
                    "SOIL_MOISTURE_TREND_V1",
                    now.minus(i, ChronoUnit.DAYS),
                    now.minus(i, ChronoUnit.DAYS).plus(24, ChronoUnit.HOURS),
                    now.minus(i, ChronoUnit.DAYS).plus(24, ChronoUnit.HOURS),
                    predicted,
                    BigDecimal.valueOf(20.0),
                    BigDecimal.valueOf(30.0),
                    actual,
                    null,
                    null,
                    null,
                    null,
                    false,
                    85,
                    EvaluationStatus.EVALUATED,
                    null,
                    EvaluationDataQuality.DATA_COMPLETE,
                    "BACKTEST_TEST",
                    "Automated unit test"
            ));
        }

        // Candidate configuration increases prediction towards actual
        String candidateConfig = "{\"smoothingAlpha\":0.25,\"trendWindowHours\":36}";

        BacktestRun run = backtestingEngine.executeBacktest(
                proposalId,
                "SOIL_MOISTURE_TREND_V1",
                "SOIL_MOISTURE_TREND_V2",
                PredictionType.SOIL_MOISTURE_FORECAST,
                candidateConfig,
                evaluations,
                now.minus(15, ChronoUnit.DAYS),
                now,
                "TEST_OPERATOR"
        );

        assertThat(run).isNotNull();
        assertThat(run.getSampleSize()).isEqualTo(12);
        assertThat(run.isHasSufficientSample()).isTrue();
        assertThat(run.getCurrentMae()).isNotNull();
        assertThat(run.getCandidateMae()).isNotNull();
        assertThat(run.getCurrentRmse()).isNotNull();
        assertThat(run.getCandidateRmse()).isNotNull();
        assertThat(run.isComparable()).isTrue();
        assertThat(run.getExecutedBy()).isEqualTo("TEST_OPERATOR");
    }
}
