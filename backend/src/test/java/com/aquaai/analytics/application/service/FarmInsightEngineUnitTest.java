package com.aquaai.analytics.application.service;

import com.aquaai.analytics.api.dto.FarmInsightDto;
import com.aquaai.analytics.api.dto.FieldPerformanceDto;
import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import com.aquaai.analytics.domain.model.InsightCategory;
import com.aquaai.analytics.domain.model.InsightSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FarmInsightEngineUnitTest {

    private FarmInsightEngine insightEngine;

    @BeforeEach
    void setUp() {
        insightEngine = new FarmInsightEngine();
    }

    @Test
    @DisplayName("Empty fields produce empty insights")
    void testEmptyFields() {
        List<FarmInsightDto> insights = insightEngine.generateInsights(Collections.emptyList());
        assertThat(insights).isEmpty();
    }

    @Test
    @DisplayName("Repeated safety incidents generate CRITICAL insight deep-linking to anomalies")
    void testRepeatedSafetyIncidentsInsight() {
        FieldPerformanceDto field = FieldPerformanceDto.builder()
                .fieldId(UUID.randomUUID())
                .fieldName("Paxtakor Dala 1")
                .cropType("COTTON")
                .soilType("LOAM")
                .areaHectares(BigDecimal.valueOf(12.5))
                .allocatedM3(BigDecimal.valueOf(10000))
                .consumedM3(BigDecimal.valueOf(7500))
                .remainingM3(BigDecimal.valueOf(2500))
                .utilizationPercent(BigDecimal.valueOf(75.0))
                .waterStatus("NORMAL")
                .plansCount(5)
                .completedOperationsCount(4)
                .planAdherencePercent(BigDecimal.valueOf(80.0))
                .telemetryCoveragePercent(BigDecimal.valueOf(95.0))
                .sensorHealth("ONLINE")
                .safetyEventCount(3)
                .criticalAnomalyCount(2) // triggers Rule 1
                .verificationCoveragePercent(BigDecimal.valueOf(100.0))
                .performanceScore(60)
                .performanceStatus(FieldPerformanceStatus.ATTENTION)
                .scoreBreakdown(Collections.emptyMap())
                .build();

        List<FarmInsightDto> insights = insightEngine.generateInsights(List.of(field));

        assertThat(insights).isNotEmpty();
        FarmInsightDto safetyInsight = insights.stream()
                .filter(i -> i.getCategory() == InsightCategory.SAFETY)
                .findFirst()
                .orElse(null);

        assertThat(safetyInsight).isNotNull();
        assertThat(safetyInsight.getSeverity()).isEqualTo(InsightSeverity.CRITICAL);
        assertThat(safetyInsight.getTargetSection()).isEqualTo("anomalies");
        assertThat(safetyInsight.getEvidence()).isNotEmpty();
    }

    @Test
    @DisplayName("Water over-consumption generates HIGH insight deep-linking to water")
    void testWaterOverConsumptionInsight() {
        FieldPerformanceDto field = FieldPerformanceDto.builder()
                .fieldId(UUID.randomUUID())
                .fieldName("Bogʻ Dala 2")
                .cropType("WHEAT")
                .soilType("SANDY_LOAM")
                .areaHectares(BigDecimal.valueOf(8.0))
                .allocatedM3(BigDecimal.valueOf(5000))
                .consumedM3(BigDecimal.valueOf(6200)) // Over-consumed!
                .remainingM3(BigDecimal.valueOf(-1200))
                .utilizationPercent(BigDecimal.valueOf(124.0)) // > 100%
                .waterStatus("OVER_CONSUMED")
                .plansCount(4)
                .completedOperationsCount(4)
                .planAdherencePercent(BigDecimal.valueOf(100.0))
                .telemetryCoveragePercent(BigDecimal.valueOf(90.0))
                .sensorHealth("ONLINE")
                .safetyEventCount(0)
                .criticalAnomalyCount(0)
                .verificationCoveragePercent(BigDecimal.valueOf(100.0))
                .performanceScore(55)
                .performanceStatus(FieldPerformanceStatus.ATTENTION)
                .scoreBreakdown(Collections.emptyMap())
                .build();

        List<FarmInsightDto> insights = insightEngine.generateInsights(List.of(field));

        FarmInsightDto waterInsight = insights.stream()
                .filter(i -> i.getCategory() == InsightCategory.WATER)
                .findFirst()
                .orElse(null);

        assertThat(waterInsight).isNotNull();
        assertThat(waterInsight.getSeverity()).isEqualTo(InsightSeverity.HIGH);
        assertThat(waterInsight.getTargetSection()).isEqualTo("water");
    }
}
