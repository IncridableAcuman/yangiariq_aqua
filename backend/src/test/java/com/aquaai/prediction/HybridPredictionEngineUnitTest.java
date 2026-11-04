package com.aquaai.prediction;

import com.aquaai.prediction.domain.service.HybridPredictionEngine;
import com.aquaai.prediction.domain.service.HybridPredictionEngine.HybridResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HybridPredictionEngineUnitTest {

    private final HybridPredictionEngine engine = new HybridPredictionEngine();

    @Test
    @DisplayName("Applies valid bounded ML residual correction (e.g. 42.0% baseline + 2.3% ML residual = 44.3%)")
    void appliesBoundedResidualCorrection() {
        HybridResult result = engine.computeHybridPrediction(
                42.0, // baseline
                44.3, // ML predicted
                5.0,  // max allowed correction
                false // no critical safety anomaly
        );

        assertThat(result.fallbackUsed()).isFalse();
        assertThat(result.appliedCorrection()).isCloseTo(2.3, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.finalPrediction()).isCloseTo(44.3, org.assertj.core.data.Offset.offset(0.01));
        assertThat(result.rationale()).contains("Bounded ML residual correction of +2.3%");
    }

    @Test
    @DisplayName("Rejects ML correction and falls back to baseline when residual exceeds agronomic limit (> 5.0%)")
    void rejectsExcessiveResidualCorrection() {
        HybridResult result = engine.computeHybridPrediction(
                42.0, // baseline
                49.5, // ML predicted residual +7.5% > 5.0%
                5.0,  // max allowed correction
                false
        );

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.appliedCorrection()).isEqualTo(0.0);
        assertThat(result.finalPrediction()).isEqualTo(42.0); // Safe baseline fallback
        assertThat(result.rationale()).contains("exceeded safe agronomic limit");
    }

    @Test
    @DisplayName("Rejects ML correction and falls back to baseline when proposed prediction breaches physical bounds (< 5% or > 55%)")
    void rejectsPhysicalBoundaryBreaches() {
        HybridResult result = engine.computeHybridPrediction(
                7.0,
                3.5, // 3.5% is below minimum 5.0%
                5.0,
                false
        );

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.finalPrediction()).isEqualTo(7.0);
        assertThat(result.rationale()).contains("breached physical moisture bounds");
    }

    @Test
    @DisplayName("Enforces critical hydraulic safety dominance unconditionally (ignoring ML)")
    void safetyInterlockDominanceOverridesMl() {
        HybridResult result = engine.computeHybridPrediction(
                35.0,
                36.5,
                5.0,
                true // CRITICAL SAFETY INTERLOCK ACTIVE
        );

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.finalPrediction()).isEqualTo(35.0);
        assertThat(result.rationale()).contains("Safety interlock active");
    }

    @Test
    @DisplayName("Falls back to baseline safely if ML candidate produces NaN or null")
    void fallsBackOnMlFailureOrNan() {
        HybridResult result = engine.computeHybridPrediction(
                30.0,
                Double.NaN,
                5.0,
                false
        );

        assertThat(result.fallbackUsed()).isTrue();
        assertThat(result.finalPrediction()).isEqualTo(30.0);
        assertThat(result.rationale()).contains("ML candidate output unavailable");
    }
}
