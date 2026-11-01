package com.aquaai.sensor.api;

import com.aquaai.sensor.api.dto.FieldAnalyticsSummaryResponse;
import com.aquaai.sensor.api.dto.SensorReadingPageResponse;
import com.aquaai.sensor.api.dto.TelemetryAggregateResponse;
import com.aquaai.sensor.application.usecase.GetFieldAnalyticsSummaryUseCase;
import com.aquaai.sensor.application.usecase.GetFieldAnalyticsUseCase;
import com.aquaai.sensor.application.usecase.GetSensorAnalyticsUseCase;
import com.aquaai.sensor.application.usecase.ListFieldReadingsUseCase;
import com.aquaai.sensor.application.usecase.ListSensorReadingsUseCase;
import com.aquaai.sensor.domain.analytics.AggregationInterval;
import com.aquaai.sensor.domain.analytics.FieldAnalyticsSummaryData;
import com.aquaai.sensor.domain.analytics.TelemetryAggregate;
import com.aquaai.sensor.domain.model.ReadingQuality;
import com.aquaai.sensor.domain.model.SensorReadingPage;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST controller exposing historical telemetry analytics, time-window aggregations, and parcel summaries.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Sensor Telemetry Analytics", description = "Time-series aggregation, quality metrics, and fleet availability")
@SecurityRequirement(name = "BearerAuth")
public class SensorAnalyticsController {

    private final GetSensorAnalyticsUseCase getSensorAnalyticsUseCase;
    private final GetFieldAnalyticsUseCase getFieldAnalyticsUseCase;
    private final GetFieldAnalyticsSummaryUseCase getFieldAnalyticsSummaryUseCase;
    private final ListSensorReadingsUseCase listSensorReadingsUseCase;
    private final ListFieldReadingsUseCase listFieldReadingsUseCase;

    @GetMapping("/sensors/{sensorId}/analytics")
    @Operation(summary = "Get aggregated time-series telemetry for a specific sensor")
    public ResponseEntity<ApiResponse<List<TelemetryAggregateResponse>>> getSensorAnalytics(
            @PathVariable UUID sensorId,
            @Parameter(description = "Start timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @Parameter(description = "Aggregation interval: 5m, 15m, 1h, 1d, 1w")
            @RequestParam(required = false, defaultValue = "1h") String interval,
            @Parameter(description = "Quality filter (VALID, SUSPECT, INVALID)")
            @RequestParam(required = false) ReadingQuality quality
    ) {
        AggregationInterval parsedInterval = AggregationInterval.fromCode(interval);
        List<TelemetryAggregate> aggregates = getSensorAnalyticsUseCase.execute(
                new GetSensorAnalyticsUseCase.Query(sensorId, from, to, parsedInterval, quality)
        );
        List<TelemetryAggregateResponse> responseList = aggregates.stream()
                .map(TelemetryAggregateResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/sensors/{sensorId}/history")
    @Operation(summary = "Get paginated raw historical telemetry for a sensor")
    public ResponseEntity<ApiResponse<SensorReadingPageResponse>> getSensorHistory(
            @PathVariable UUID sensorId,
            @Parameter(description = "Start timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        SensorReadingPage readingPage = listSensorReadingsUseCase.execute(
                new ListSensorReadingsUseCase.Query(sensorId, from, to, page, size, sort)
        );
        return ResponseEntity.ok(ApiResponse.ok(SensorReadingPageResponse.from(readingPage)));
    }

    @GetMapping("/fields/{fieldId}/analytics")
    @Operation(summary = "Get aggregated time-series telemetry across sensors on a field")
    public ResponseEntity<ApiResponse<List<TelemetryAggregateResponse>>> getFieldAnalytics(
            @PathVariable UUID fieldId,
            @Parameter(description = "Filter by sensor modality")
            @RequestParam(required = false) SensorType sensorType,
            @Parameter(description = "Start timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @Parameter(description = "Aggregation interval: 5m, 15m, 1h, 1d, 1w")
            @RequestParam(required = false, defaultValue = "1h") String interval,
            @Parameter(description = "Quality filter (VALID, SUSPECT, INVALID)")
            @RequestParam(required = false) ReadingQuality quality
    ) {
        AggregationInterval parsedInterval = AggregationInterval.fromCode(interval);
        List<TelemetryAggregate> aggregates = getFieldAnalyticsUseCase.execute(
                new GetFieldAnalyticsUseCase.Query(fieldId, sensorType, from, to, parsedInterval, quality)
        );
        List<TelemetryAggregateResponse> responseList = aggregates.stream()
                .map(TelemetryAggregateResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responseList));
    }

    @GetMapping("/fields/{fieldId}/history")
    @Operation(summary = "Get paginated raw historical telemetry for a field")
    public ResponseEntity<ApiResponse<SensorReadingPageResponse>> getFieldHistory(
            @PathVariable UUID fieldId,
            @Parameter(description = "Start timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "End timestamp (ISO-8601 UTC)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "desc") String sort
    ) {
        SensorReadingPage readingPage = listFieldReadingsUseCase.execute(
                new ListFieldReadingsUseCase.Query(fieldId, from, to, page, size, sort)
        );
        return ResponseEntity.ok(ApiResponse.ok(SensorReadingPageResponse.from(readingPage)));
    }

    @GetMapping("/fields/{fieldId}/analytics/summary")
    @Operation(summary = "Get executive operational analytics summary for a field")
    public ResponseEntity<ApiResponse<FieldAnalyticsSummaryResponse>> getFieldAnalyticsSummary(
            @PathVariable UUID fieldId
    ) {
        FieldAnalyticsSummaryData summaryData = getFieldAnalyticsSummaryUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(FieldAnalyticsSummaryResponse.from(summaryData)));
    }
}
