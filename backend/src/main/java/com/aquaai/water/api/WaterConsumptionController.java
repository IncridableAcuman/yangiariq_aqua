package com.aquaai.water.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.water.api.dto.FieldWaterSummaryResponse;
import com.aquaai.water.api.dto.IrrigationPlanMetricsResponse;
import com.aquaai.water.api.dto.WaterAllocationConsumptionResponse;
import com.aquaai.water.application.usecase.GetFieldWaterSummaryUseCase;
import com.aquaai.water.application.usecase.GetIrrigationPlanMetricsUseCase;
import com.aquaai.water.application.usecase.GetWaterAllocationConsumptionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.UUID;

/**
 * REST controller for water consumption analytics, field quota summaries,
 * and planned vs actual delivery variance metrics.
 */
@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
@Tag(name = "Water Consumption & Analytics", description = "Endpoints for deriving field volumetric summaries, quota balances, over-consumption flags, and planned vs actual ratios")
@SecurityRequirement(name = "bearerAuth")
public class WaterConsumptionController {

    private final GetFieldWaterSummaryUseCase getFieldWaterSummaryUseCase;
    private final GetWaterAllocationConsumptionUseCase getWaterAllocationConsumptionUseCase;
    private final GetIrrigationPlanMetricsUseCase getIrrigationPlanMetricsUseCase;

    @GetMapping("/fields/{fieldId}/summary")
    @Operation(
            summary = "Get field periodic water consumption summary",
            description = "Calculates total allocated quotas, total delivered water, remaining balances, and over-consumption indicators. Enforces ownership: Farmers access only owned parcels (404 anti-enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field water summary retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - e.g. 'from' timestamp after 'to' timestamp")
    })
    public ResponseEntity<ApiResponse<FieldWaterSummaryResponse>> getFieldWaterSummary(
            @Parameter(description = "Agricultural parcel field identifier")
            @PathVariable UUID fieldId,

            @Parameter(description = "Optional window start timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,

            @Parameter(description = "Optional window end timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        FieldWaterSummaryResponse response = getFieldWaterSummaryUseCase.execute(fieldId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/allocations/{id}/summary")
    @Operation(
            summary = "Get water allocation consumption and quota balance summary",
            description = "Calculates total water delivered under this quota, remaining balance, utilization percentage, and over-consumption indicators."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Allocation consumption summary retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Allocation not found or not accessible")
    })
    public ResponseEntity<ApiResponse<WaterAllocationConsumptionResponse>> getAllocationSummary(
            @Parameter(description = "Water allocation quota identifier")
            @PathVariable UUID id
    ) {
        WaterAllocationConsumptionResponse response = getWaterAllocationConsumptionUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/plans/{id}/metrics")
    @Operation(
            summary = "Get planned vs actual delivery variance metrics for an irrigation plan",
            description = "Calculates volumetric variance, percentage deviation, and operational consumption ratio between scheduled intention and actual execution."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan metrics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanMetricsResponse>> getPlanMetrics(
            @Parameter(description = "Irrigation plan scheduled identifier")
            @PathVariable UUID id
    ) {
        IrrigationPlanMetricsResponse response = getIrrigationPlanMetricsUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
