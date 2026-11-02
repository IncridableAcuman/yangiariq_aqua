package com.aquaai.prediction.domain.service;

import com.aquaai.prediction.domain.model.PredictionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegressionGuardService Domain Service Unit Tests")
class RegressionGuardUnitTest {

    private RegressionGuardService regressionGuardService;

    @BeforeEach
    void setUp() {
        regressionGuardService = new RegressionGuardService();
    }

    @Test
    @DisplayName("Should pass when candidate improves MAE without bias inflation or accuracy regression")
    void shouldPassWhenCandidateImprovesHealthily() {
        RegressionGuardService.RegressionCheckResult result = regressionGuardService.evaluateRegression(
                PredictionType.SOIL_MOISTURE_FORECAST,
                BigDecimal.valueOf(4.50), // current MAE
                BigDecimal.valueOf(3.20), // candidate MAE (improved)
                BigDecimal.valueOf(-1.20), // current MBE
                BigDecimal.valueOf(-0.80), // candidate MBE (improved)
                BigDecimal.valueOf(88.0),  // current accuracy
                BigDecimal.valueOf(91.0),  // candidate accuracy
                BigDecimal.valueOf(0.85),  // current F1
                BigDecimal.valueOf(0.88)   // candidate F1
        );

        assertThat(result.regressionDetected()).isFalse();
        assertThat(result.details()).contains("No regressions detected");
    }

    @Test
    @DisplayName("Should detect regression when candidate inflates bias (MBE)")
    void shouldDetectBiasInflationRegression() {
        RegressionGuardService.RegressionCheckResult result = regressionGuardService.evaluateRegression(
                PredictionType.SOIL_MOISTURE_FORECAST,
                BigDecimal.valueOf(4.50),
                BigDecimal.valueOf(4.00), // slightly better MAE
                BigDecimal.valueOf(1.00),  // current MBE
                BigDecimal.valueOf(5.50),  // candidate MBE (inflated by > 3.0)
                BigDecimal.valueOf(88.0),
                BigDecimal.valueOf(88.0),
                BigDecimal.valueOf(0.85),
                BigDecimal.valueOf(0.85)
        );

        assertThat(result.regressionDetected()).isTrue();
        assertThat(result.details()).contains("Candidate systematically inflates bias");
    }

    @Test
    @DisplayName("Should detect regression when candidate drops classification accuracy by > 5%")
    void shouldDetectAccuracyDropRegression() {
        RegressionGuardService.RegressionCheckResult result = regressionGuardService.evaluateRegression(
                PredictionType.IRRIGATION_NEED_FORECAST,
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(1.8),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(90.0), // current accuracy
                BigDecimal.valueOf(82.0), // candidate drops by 8% (> 5%)
                BigDecimal.valueOf(0.85),
                BigDecimal.valueOf(0.75)  // F1 drops by 0.10 (> 0.08)
        );

        assertThat(result.regressionDetected()).isTrue();
        assertThat(result.details()).contains("reduces classification accuracy");
        assertThat(result.details()).contains("reduces F1 score");
    }

    @Test
    @DisplayName("Should detect regression when candidate MAE is worse than current MAE")
    void shouldDetectMaeWorseningRegression() {
        RegressionGuardService.RegressionCheckResult result = regressionGuardService.evaluateRegression(
                PredictionType.WATER_DEMAND_FORECAST,
                BigDecimal.valueOf(12.5), // current MAE
                BigDecimal.valueOf(15.2), // candidate MAE is worse
                BigDecimal.valueOf(2.0),
                BigDecimal.valueOf(2.1),
                null, null, null, null
        );

        assertThat(result.regressionDetected()).isTrue();
        assertThat(result.details()).contains("increases Mean Absolute Error");
    }
}
