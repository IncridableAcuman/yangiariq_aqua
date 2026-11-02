package com.aquaai.analytics.api;

import com.aquaai.analytics.api.dto.AnalyticsOverviewResponse;
import com.aquaai.analytics.api.dto.FarmInsightDto;
import com.aquaai.analytics.api.dto.FieldPerformanceRankingResponse;
import com.aquaai.analytics.api.dto.IrrigationPerformanceDto;
import com.aquaai.analytics.api.dto.PerformanceTrendsResponse;
import com.aquaai.analytics.api.dto.TelemetryQualityDto;
import com.aquaai.analytics.api.dto.VerificationPerformanceDto;
import com.aquaai.analytics.api.dto.WaterPerformanceDto;
import com.aquaai.analytics.application.usecase.GetFarmInsightsUseCase;
import com.aquaai.analytics.application.usecase.GetFarmPerformanceAnalyticsUseCase;
import com.aquaai.analytics.application.usecase.GetFieldPerformanceRankingUseCase;
import com.aquaai.analytics.application.usecase.GetPerformanceTrendsUseCase;
import com.aquaai.analytics.domain.model.FieldPerformanceStatus;
import com.aquaai.analytics.domain.model.InsightCategory;
import com.aquaai.analytics.domain.model.InsightSeverity;
import com.aquaai.analytics.domain.model.PerformancePeriodType;
import com.aquaai.analytics.domain.model.TrendGranularity;
import com.aquaai.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for farm performance, historical intelligence, and analytics.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final GetFarmPerformanceAnalyticsUseCase getFarmPerformanceAnalyticsUseCase;
    private final GetPerformanceTrendsUseCase getPerformanceTrendsUseCase;
    private final GetFieldPerformanceRankingUseCase getFieldPerformanceRankingUseCase;
    private final GetFarmInsightsUseCase getFarmInsightsUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AnalyticsOverviewResponse>> getOverview(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String cropType
    ) {
        AnalyticsOverviewResponse response = getFarmPerformanceAnalyticsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                cropType
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/water")
    public ResponseEntity<ApiResponse<WaterPerformanceDto>> getWaterPerformance(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String cropType
    ) {
        AnalyticsOverviewResponse response = getFarmPerformanceAnalyticsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                cropType
        );
        return ResponseEntity.ok(ApiResponse.ok(response.getWaterPerformance()));
    }

    @GetMapping("/irrigation")
    public ResponseEntity<ApiResponse<IrrigationPerformanceDto>> getIrrigationPerformance(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String cropType
    ) {
        AnalyticsOverviewResponse response = getFarmPerformanceAnalyticsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                cropType
        );
        return ResponseEntity.ok(ApiResponse.ok(response.getIrrigationPerformance()));
    }

    @GetMapping("/telemetry")
    public ResponseEntity<ApiResponse<TelemetryQualityDto>> getTelemetryQuality(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String cropType
    ) {
        AnalyticsOverviewResponse response = getFarmPerformanceAnalyticsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                cropType
        );
        return ResponseEntity.ok(ApiResponse.ok(response.getTelemetryQuality()));
    }

    @GetMapping("/verification")
    public ResponseEntity<ApiResponse<VerificationPerformanceDto>> getVerificationPerformance(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) String cropType
    ) {
        AnalyticsOverviewResponse response = getFarmPerformanceAnalyticsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                cropType
        );
        return ResponseEntity.ok(ApiResponse.ok(response.getVerificationPerformance()));
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<PerformanceTrendsResponse>> getTrends(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) TrendGranularity granularity
    ) {
        PerformanceTrendsResponse response = getPerformanceTrendsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                granularity
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<FieldPerformanceRankingResponse>> getFieldRankings(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) String cropType,
            @RequestParam(required = false) FieldPerformanceStatus status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "score") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection
    ) {
        FieldPerformanceRankingResponse response = getFieldPerformanceRankingUseCase.execute(
                period,
                from,
                to,
                cropType,
                status,
                page,
                size,
                sortBy,
                sortDirection
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<List<FarmInsightDto>>> getInsights(
            @RequestParam(required = false, defaultValue = "LAST_30_DAYS") PerformancePeriodType period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) InsightCategory category,
            @RequestParam(required = false) InsightSeverity severity
    ) {
        List<FarmInsightDto> response = getFarmInsightsUseCase.execute(
                period,
                from,
                to,
                fieldId,
                category,
                severity
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
