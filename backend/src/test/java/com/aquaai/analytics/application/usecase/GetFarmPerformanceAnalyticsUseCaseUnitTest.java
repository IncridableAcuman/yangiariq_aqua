package com.aquaai.analytics.application.usecase;

import com.aquaai.analytics.api.dto.AnalyticsOverviewResponse;
import com.aquaai.analytics.application.service.FarmInsightEngine;
import com.aquaai.analytics.application.service.PerformanceScoringService;
import com.aquaai.analytics.application.service.PeriodResolver;
import com.aquaai.analytics.domain.model.DataConfidenceLevel;
import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.analytics.domain.port.AnalyticsQueryPort;
import com.aquaai.field.application.FieldOwnershipChecker;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorStatus;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import com.aquaai.water.domain.model.FieldWaterSummaryData;
import com.aquaai.water.domain.model.IrrigationEventStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFarmPerformanceAnalyticsUseCaseUnitTest {

    @Mock
    private AnalyticsQueryPort queryPort;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FieldOwnershipChecker fieldOwnershipChecker;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private PerformanceScoringService scoringService = new PerformanceScoringService();

    @Spy
    private FarmInsightEngine insightEngine = new FarmInsightEngine();

    @Spy
    private PeriodResolver periodResolver = new PeriodResolver();

    @InjectMocks
    private GetFarmPerformanceAnalyticsUseCase useCase;

    private UUID userId;
    private UUID fieldId;
    private Field mockField;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        fieldId = UUID.randomUUID();

        mockField = Field.create(
                fieldId,
                userId,
                "Oltin Voha Dala 1",
                "Description",
                BigDecimal.valueOf(15.0),
                CropType.COTTON,
                SoilType.LOAM,
                IrrigationMethod.DRIP,
                null,
                null
        );
    }

    @Test
    @DisplayName("Calculate complete overview metrics correctly when data is available")
    void testExecuteWithCompleteData() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(userId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldRepository.findByOwnerId(userId)).thenReturn(List.of(mockField));

        // Mock water summary: 10,000 allocated, 1 allocation, 7,500 consumed, 4 events
        FieldWaterSummaryData summary = new FieldWaterSummaryData(
                BigDecimal.valueOf(10000),
                1L,
                BigDecimal.valueOf(7500),
                4L
        );
        when(queryPort.getFieldWaterSummaries(any(), any(), any())).thenReturn(Map.of(fieldId, summary));

        // Mock planned volumes: 7,000 m3
        when(queryPort.getPlannedVolumes(any(), any(), any())).thenReturn(Map.of(fieldId, BigDecimal.valueOf(7000)));

        // Mock delivered volumes: 7,500 m3
        when(queryPort.getDeliveredVolumes(any(), any(), any())).thenReturn(Map.of(fieldId, BigDecimal.valueOf(7500)));

        // Mock plans count: 5 completed
        when(queryPort.getPlanStatusCounts(any(), any(), any())).thenReturn(Map.of(
                fieldId, Map.of(IrrigationPlanStatus.COMPLETED, 5L)
        ));

        // Mock events count: 5 completed
        when(queryPort.getEventStatusCounts(any(), any(), any())).thenReturn(Map.of(
                fieldId, Map.of(IrrigationEventStatus.COMPLETED, 5L)
        ));

        // Mock average duration
        when(queryPort.getAverageActualDurationsMinutes(any(), any(), any())).thenReturn(Map.of(fieldId, 120));

        // Mock anomalies: 0
        when(queryPort.getAnomalyCounts(any(), any(), any())).thenReturn(Map.of(fieldId, Collections.emptyMap()));

        // Mock sensor statuses: 3 active
        when(queryPort.getSensorStatusCounts(any())).thenReturn(Map.of(
                fieldId, Map.of(SensorStatus.ACTIVE, 3)
        ));

        // Mock readings quality: 100 valid out of 100
        when(queryPort.getReadingQualityCounts(any(), any(), any())).thenReturn(Map.of(
                fieldId, Map.of(ReadingQuality.VALID, 100L)
        ));

        // Mock verified operations: 5 verified
        when(queryPort.getVerifiedOperationsCount(any(), any(), any())).thenReturn(Map.of(fieldId, 5L));

        AnalyticsOverviewResponse response = useCase.execute(
                PerformancePeriodType.LAST_30_DAYS,
                null,
                null,
                null,
                null
        );

        assertThat(response).isNotNull();

        // 1. Water Performance verification
        assertThat(response.getWaterPerformance().getTotalAllocatedM3()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.getWaterPerformance().getTotalConsumedM3()).isEqualByComparingTo(BigDecimal.valueOf(7500));
        assertThat(response.getWaterPerformance().getUtilizationPercent()).isEqualByComparingTo(BigDecimal.valueOf(75.0));
        assertThat(response.getWaterPerformance().getUtilizationStatus()).isEqualTo("NORMAL");
        // Planned: 7000, Actual: 7500 -> variance: +500
        assertThat(response.getWaterPerformance().getVarianceM3()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.getWaterPerformance().getOverConsumedVolumeM3()).isEqualByComparingTo(BigDecimal.ZERO);

        // 2. Irrigation Performance verification
        assertThat(response.getIrrigationPerformance().getCompletedCount()).isEqualTo(5);
        assertThat(response.getIrrigationPerformance().getOperationCompletionRate()).isGreaterThan(BigDecimal.ZERO);

        // 3. Telemetry Quality verification
        assertThat(response.getTelemetryQuality().getTelemetryCoveragePercent()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(response.getTelemetryQuality().getDataConfidence()).isEqualTo(DataConfidenceLevel.COMPLETE);

        // 4. Verification Coverage
        assertThat(response.getVerificationPerformance().getVerificationCoveragePercent()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(response.getVerificationPerformance().getDataConfidence()).isEqualTo(DataConfidenceLevel.COMPLETE);

        // 5. Data confidence overall
        assertThat(response.getDataConfidence().getOverall()).isEqualTo(DataConfidenceLevel.COMPLETE);
        assertThat(response.getDataConfidence().isHasIncompleteData()).isFalse();
    }

    @Test
    @DisplayName("Over-consumption (>100% quota) correctly triggers OVER_CONSUMED status and insight")
    void testOverConsumptionStatusAndInsight() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(userId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldRepository.findByOwnerId(userId)).thenReturn(List.of(mockField));

        // Mock water summary: 5,000 allocated, 1 allocation, 6,200 consumed, 3 events (Over-consumed!)
        FieldWaterSummaryData summary = new FieldWaterSummaryData(
                BigDecimal.valueOf(5000),
                1L,
                BigDecimal.valueOf(6200),
                3L
        );
        when(queryPort.getFieldWaterSummaries(any(), any(), any())).thenReturn(Map.of(fieldId, summary));
        when(queryPort.getPlannedVolumes(any(), any(), any())).thenReturn(Map.of(fieldId, BigDecimal.valueOf(5000)));
        when(queryPort.getDeliveredVolumes(any(), any(), any())).thenReturn(Map.of(fieldId, BigDecimal.valueOf(6200)));
        when(queryPort.getPlanStatusCounts(any(), any(), any())).thenReturn(Map.of(fieldId, Collections.emptyMap()));
        when(queryPort.getEventStatusCounts(any(), any(), any())).thenReturn(Map.of(
                fieldId, Map.of(IrrigationEventStatus.COMPLETED, 3L)
        ));
        when(queryPort.getAverageActualDurationsMinutes(any(), any(), any())).thenReturn(Map.of(fieldId, 100));
        when(queryPort.getAnomalyCounts(any(), any(), any())).thenReturn(Map.of(fieldId, Collections.emptyMap()));
        when(queryPort.getSensorStatusCounts(any())).thenReturn(Map.of(fieldId, Collections.emptyMap()));
        when(queryPort.getReadingQualityCounts(any(), any(), any())).thenReturn(Map.of(fieldId, Collections.emptyMap()));
        when(queryPort.getVerifiedOperationsCount(any(), any(), any())).thenReturn(Map.of(fieldId, 3L));

        AnalyticsOverviewResponse response = useCase.execute(
                PerformancePeriodType.LAST_30_DAYS,
                null,
                null,
                null,
                null
        );

        assertThat(response.getWaterPerformance().getUtilizationStatus()).isEqualTo("OVER_CONSUMED");
        assertThat(response.getWaterPerformance().getOverConsumedVolumeM3()).isEqualByComparingTo(BigDecimal.valueOf(1200));
        assertThat(response.getWaterPerformance().getOverConsumedFieldCount()).isEqualTo(1);

        // Insights must contain water over-consumption warning
        assertThat(response.getTopInsights()).anyMatch(i -> i.getTargetSection().equals("water"));
    }

    @Test
    @DisplayName("Missing previous period data returns hasComparisonData=false and safe fallback message")
    void testInsufficientHistoricalDataComparison() {
        when(currentUserProvider.getRequiredUserId()).thenReturn(userId);
        when(currentUserProvider.getRequiredUserRole()).thenReturn(UserRole.FARMER);
        when(fieldRepository.findByOwnerId(userId)).thenReturn(List.of(mockField));

        when(queryPort.getFieldWaterSummaries(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getPlannedVolumes(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getDeliveredVolumes(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getPlanStatusCounts(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getEventStatusCounts(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getAverageActualDurationsMinutes(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getAnomalyCounts(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getSensorStatusCounts(any())).thenReturn(Collections.emptyMap());
        when(queryPort.getReadingQualityCounts(any(), any(), any())).thenReturn(Collections.emptyMap());
        when(queryPort.getVerifiedOperationsCount(any(), any(), any())).thenReturn(Collections.emptyMap());

        AnalyticsOverviewResponse response = useCase.execute(
                PerformancePeriodType.LAST_30_DAYS,
                null,
                null,
                null,
                null
        );

        assertThat(response.getPeriodComparison().isHasComparisonData()).isFalse();
        assertThat(response.getPeriodComparison().getComparisonSummary()).contains("Insufficient historical data");
    }
}
