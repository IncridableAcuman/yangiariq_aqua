package com.aquaai.water.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.CreateIrrigationPlanRequest;
import com.aquaai.water.api.dto.IrrigationPlanResponse;
import com.aquaai.water.api.dto.UpdateIrrigationPlanRequest;
import com.aquaai.water.application.usecase.CancelIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.CompleteIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.CreateIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.GetIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.ListIrrigationPlansUseCase;
import com.aquaai.water.application.usecase.StartIrrigationPlanUseCase;
import com.aquaai.water.application.usecase.UpdateIrrigationPlanUseCase;
import com.aquaai.water.domain.model.IrrigationPlanStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for scheduling, tracking, and managing agricultural parcel irrigation plans.
 * Enforces field ownership, cross-aggregate allocation validation, and lifecycle state machines.
 */
@RestController
@RequestMapping("/api/v1/irrigation-plans")
@RequiredArgsConstructor
@Tag(name = "Irrigation Scheduling", description = "Endpoints for scheduling irrigation plans, technique profiling, and status execution lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class IrrigationPlanController {

    private final CreateIrrigationPlanUseCase createIrrigationPlanUseCase;
    private final GetIrrigationPlanUseCase getIrrigationPlanUseCase;
    private final ListIrrigationPlansUseCase listIrrigationPlansUseCase;
    private final UpdateIrrigationPlanUseCase updateIrrigationPlanUseCase;
    private final StartIrrigationPlanUseCase startIrrigationPlanUseCase;
    private final CompleteIrrigationPlanUseCase completeIrrigationPlanUseCase;
    private final CancelIrrigationPlanUseCase cancelIrrigationPlanUseCase;
    private final com.aquaai.water.application.usecase.GetIrrigationPlanMetricsUseCase getIrrigationPlanMetricsUseCase;

    @PostMapping
    @Operation(
            summary = "Schedule a new irrigation plan",
            description = "Creates a planned watering schedule for a parcel. Validates field ownership and cross-aggregate quota consistency."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Irrigation plan scheduled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field or allocation not found (anti-enumeration for unowned fields)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - quota mismatch or inactive allocation")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> createIrrigationPlan(
            @Valid @RequestBody CreateIrrigationPlanRequest request
    ) {
        IrrigationPlanResponse response = createIrrigationPlanUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get irrigation plan details by ID",
            description = "Retrieves plan details. Enforces ownership: Farmers access only plans for parcels they own (404 anti-enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> getIrrigationPlanById(
            @PathVariable UUID id
    ) {
        IrrigationPlanResponse response = getIrrigationPlanUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/metrics")
    @Operation(
            summary = "Get planned vs actual delivery variance metrics for an irrigation plan",
            description = "Retrieves volumetric variance, percentage deviation, and operational consumption ratio between scheduled target and actual delivery."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan metrics retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible")
    })
    public ResponseEntity<ApiResponse<com.aquaai.water.api.dto.IrrigationPlanMetricsResponse>> getIrrigationPlanMetrics(
            @PathVariable UUID id
    ) {
        com.aquaai.water.api.dto.IrrigationPlanMetricsResponse response = getIrrigationPlanMetricsUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(
            summary = "List irrigation plans with filtering and pagination",
            description = "For FARMER, lists plans strictly for owned parcels. For OPERATOR and ADMIN, lists plans district-wide with optional field filtering."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plans retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified field not found or unowned")
    })
    public ResponseEntity<ApiResponse<PageResponse<IrrigationPlanResponse>>> listIrrigationPlans(
            @Parameter(description = "Filter by parcel field ID")
            @RequestParam(required = false) UUID fieldId,

            @Parameter(description = "Filter by water allocation quota ID")
            @RequestParam(required = false) UUID waterAllocationId,

            @Parameter(description = "Filter by plan status")
            @RequestParam(required = false) IrrigationPlanStatus status,

            @Parameter(description = "Filter window start timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,

            @Parameter(description = "Filter window end timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort property", example = "plannedStartAt")
            @RequestParam(defaultValue = "plannedStartAt") String sortBy,

            @Parameter(description = "Sort direction ('asc' or 'desc')", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResponse<IrrigationPlanResponse> response = listIrrigationPlansUseCase.execute(
                fieldId,
                waterAllocationId,
                status,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update planned irrigation schedule details",
            description = "Updates volume, time window, or method. Allowed only while in PLANNED status."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - plan not in PLANNED status")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> updateIrrigationPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIrrigationPlanRequest request
    ) {
        IrrigationPlanResponse response = updateIrrigationPlanUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/start")
    @Operation(
            summary = "Start scheduled irrigation delivery",
            description = "Transitions irrigation plan from PLANNED to IN_PROGRESS."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan transitioned to IN_PROGRESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - plan not in PLANNED status")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> startIrrigationPlan(
            @PathVariable UUID id
    ) {
        IrrigationPlanResponse response = startIrrigationPlanUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/complete")
    @Operation(
            summary = "Complete an in-progress irrigation plan",
            description = "Transitions irrigation plan from IN_PROGRESS to COMPLETED."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan transitioned to COMPLETED"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - plan not in IN_PROGRESS status")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> completeIrrigationPlan(
            @PathVariable UUID id
    ) {
        IrrigationPlanResponse response = completeIrrigationPlanUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an irrigation plan",
            description = "Transitions irrigation plan to CANCELLED. Allowed from PLANNED or IN_PROGRESS status."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation plan cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation plan not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - cannot cancel already completed plan")
    })
    public ResponseEntity<ApiResponse<IrrigationPlanResponse>> cancelIrrigationPlan(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelOperationRequest request
    ) {
        IrrigationPlanResponse response = cancelIrrigationPlanUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
