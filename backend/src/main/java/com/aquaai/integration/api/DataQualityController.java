package com.aquaai.integration.api;

import com.aquaai.integration.application.dto.DataQualitySummaryDto;
import com.aquaai.integration.application.usecase.GetDataQualityMetricsUseCase;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data-quality")
@RequiredArgsConstructor
@Tag(name = "Data Quality & Reliability", description = "Endpoints for observing data quality health, anomalies, and provider reliability metrics")
@SecurityRequirement(name = "bearerAuth")
public class DataQualityController {

    private final GetDataQualityMetricsUseCase getDataQualityMetricsUseCase;

    @GetMapping("/summary")
    @Operation(summary = "Get data quality metrics summary", description = "Aggregates valid, duplicate, stale, out-of-range, and unverified telemetry counts")
    public ResponseEntity<ApiResponse<DataQualitySummaryDto>> getQualitySummary(
            @RequestParam(defaultValue = "30") Integer days
    ) {
        DataQualitySummaryDto summary = getDataQualityMetricsUseCase.execute(days);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}
