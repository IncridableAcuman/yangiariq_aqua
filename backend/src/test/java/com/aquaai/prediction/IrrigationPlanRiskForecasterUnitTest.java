package com.aquaai.prediction;

import com.aquaai.prediction.domain.model.DataSufficiency;
import com.aquaai.prediction.domain.model.Prediction;
import com.aquaai.prediction.domain.model.PredictionHorizon;
import com.aquaai.prediction.domain.model.RiskLevel;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.prediction.domain.port.PredictionDataProvider.HydraulicAnomalyInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.UpcomingPlanInfo;
import com.aquaai.prediction.domain.port.PredictionDataProvider.WaterBalance;
import com.aquaai.prediction.domain.service.DataSufficiencyEvaluator.SufficiencyEvaluation;
import com.aquaai.prediction.domain.service.IrrigationPlanRiskForecaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationPlanRiskForecasterUnitTest {

    private IrrigationPlanRiskForecaster forecaster;
    private FieldMetadata field;
    private WaterBalance waterBalance;
    private SufficiencyEvaluation sufficiency;
    private Instant now;

    @BeforeEach
    void setUp() {
        forecaster = new IrrigationPlanRiskForecaster();
        now = Instant.now();
        field = new FieldMetadata(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Field 1 - Orchard",
                "COTTON",
                "LOAM",
                "DRIP",
                BigDecimal.valueOf(8.0),
                true
        );
        waterBalance = new WaterBalance(
                UUID.randomUUID(),
                BigDecimal.valueOf(10000.0),
                BigDecimal.valueOf(6000.0),
                BigDecimal.valueOf(4000.0),
                BigDecimal.ZERO,
                false
        );
        sufficiency = new SufficiencyEvaluation(
                DataSufficiency.SUFFICIENT,
                10, 10, 0, 0,
                Duration.ofHours(2),
                90,
                "Sufficient"
        );
    }

    @Test
    @DisplayName("Active pipe burst anomaly strictly blocks irrigation plan with BLOCKED risk")
    void pipeBurstStrictlyBlocksPlanRisk() {
        List<UpcomingPlanInfo> plans = List.of(
                new UpcomingPlanInfo(UUID.randomUUID(), field.fieldId(), now.plus(Duration.ofHours(6)), BigDecimal.valueOf(500.0), "SCHEDULED", false, null)
        );
        List<HydraulicAnomalyInfo> anomalies = List.of(
                new HydraulicAnomalyInfo(UUID.randomUUID(), field.fieldId(), "SUSPECTED_PIPE_BURST", "CRITICAL", now.minusSeconds(300), false)
        );

        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, plans, anomalies, waterBalance, sufficiency, now);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.BLOCKED);
        assertThat(result.getEvidence()).contains("Hydraulic safety interlock active");
        assertThat(result.getRecommendedAction()).contains("Inspect hydraulic infrastructure");
    }

    @Test
    @DisplayName("Upcoming plan requiring volume greater than remaining quota triggers HIGH risk")
    void quotaExhaustionTriggersHighRisk() {
        // Remaining is 4000, plan requires 4500
        List<UpcomingPlanInfo> plans = List.of(
                new UpcomingPlanInfo(UUID.randomUUID(), field.fieldId(), now.plus(Duration.ofHours(12)), BigDecimal.valueOf(4500.0), "SCHEDULED", false, null)
        );

        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, plans, Collections.emptyList(), waterBalance, sufficiency, now);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.HIGH);
        assertThat(result.getEvidence()).contains("exceeds remaining quota balance");
    }

    @Test
    @DisplayName("Plan scheduled within quota headroom and no anomalies results in LOW risk")
    void normalScheduledPlanResultsInLowRisk() {
        List<UpcomingPlanInfo> plans = List.of(
                new UpcomingPlanInfo(UUID.randomUUID(), field.fieldId(), now.plus(Duration.ofHours(12)), BigDecimal.valueOf(800.0), "SCHEDULED", false, null)
        );

        Prediction result = forecaster.forecast(field, PredictionHorizon.NEXT_24_HOURS, plans, Collections.emptyList(), waterBalance, sufficiency, now);

        assertThat(result.getRiskLevel()).isEqualTo(RiskLevel.LOW);
    }
}
