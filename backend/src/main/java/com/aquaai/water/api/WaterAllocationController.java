package com.aquaai.water.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.CreateWaterAllocationRequest;
import com.aquaai.water.api.dto.UpdateWaterAllocationRequest;
import com.aquaai.water.api.dto.WaterAllocationResponse;
import com.aquaai.water.application.usecase.CancelWaterAllocationUseCase;
import com.aquaai.water.application.usecase.CloseWaterAllocationUseCase;
import com.aquaai.water.application.usecase.CreateWaterAllocationUseCase;
import com.aquaai.water.application.usecase.GetWaterAllocationUseCase;
import com.aquaai.water.application.usecase.ListWaterAllocationsUseCase;
import com.aquaai.water.application.usecase.UpdateWaterAllocationUseCase;
import com.aquaai.water.domain.model.WaterAllocationStatus;
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
 * REST controller for managing parcel volumetric water quota allocations.
 * Enforces field ownership, anti-enumeration (404), and lifecycle state transitions.
 */
@RestController
@RequestMapping("/api/v1/water-allocations")
@RequiredArgsConstructor
@Tag(name = "Water Allocation Quotas", description = "Endpoints for parcel seasonal water quotas, volume allocations, and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class WaterAllocationController {

    private final CreateWaterAllocationUseCase createWaterAllocationUseCase;
    private final GetWaterAllocationUseCase getWaterAllocationUseCase;
    private final ListWaterAllocationsUseCase listWaterAllocationsUseCase;
    private final UpdateWaterAllocationUseCase updateWaterAllocationUseCase;
    private final CloseWaterAllocationUseCase closeWaterAllocationUseCase;
    private final CancelWaterAllocationUseCase cancelWaterAllocationUseCase;
    private final com.aquaai.water.application.usecase.GetWaterAllocationConsumptionUseCase getWaterAllocationConsumptionUseCase;

    @PostMapping
    @Operation(
            summary = "Create a parcel water allocation quota",
            description = "Establishes a volumetric quota for an agricultural parcel from an active water source. Enforces ownership for farmers."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Water allocation created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field or water source not found (anti-enumeration for unowned fields)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - e.g. inactive water source")
    })
    public ResponseEntity<ApiResponse<WaterAllocationResponse>> createWaterAllocation(
            @Valid @RequestBody CreateWaterAllocationRequest request
    ) {
        WaterAllocationResponse response = createWaterAllocationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get water allocation details by ID",
            description = "Retrieves allocation quota details. Enforces ownership: Farmers access only allocations for parcels they own (404 anti-enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocation retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water allocation not found or not accessible")
    })
    public ResponseEntity<ApiResponse<WaterAllocationResponse>> getWaterAllocationById(
            @PathVariable UUID id
    ) {
        WaterAllocationResponse response = getWaterAllocationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/consumption")
    @Operation(
            summary = "Get water allocation consumption and quota balance summary",
            description = "Retrieves real-time volumetric consumption, remaining balance, utilization percentage, and over-consumption indicators."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocation consumption retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water allocation not found or not accessible")
    })
    public ResponseEntity<ApiResponse<com.aquaai.water.api.dto.WaterAllocationConsumptionResponse>> getWaterAllocationConsumption(
            @PathVariable UUID id
    ) {
        com.aquaai.water.api.dto.WaterAllocationConsumptionResponse response = getWaterAllocationConsumptionUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(
            summary = "List water allocations with filtering and pagination",
            description = "For FARMER, lists quotas strictly for owned parcels. For OPERATOR and ADMIN, lists quotas district-wide with optional field filtering."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocations retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified field not found or unowned")
    })
    public ResponseEntity<ApiResponse<PageResponse<WaterAllocationResponse>>> listWaterAllocations(
            @Parameter(description = "Filter by parcel field ID")
            @RequestParam(required = false) UUID fieldId,

            @Parameter(description = "Filter by water source ID")
            @RequestParam(required = false) UUID waterSourceId,

            @Parameter(description = "Filter by allocation status")
            @RequestParam(required = false) WaterAllocationStatus status,

            @Parameter(description = "Filter window start timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodStart,

            @Parameter(description = "Filter window end timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant periodEnd,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort property", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction ('asc' or 'desc')", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResponse<WaterAllocationResponse> response = listWaterAllocationsUseCase.execute(
                fieldId,
                waterSourceId,
                status,
                periodStart,
                periodEnd,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update active water allocation quota details",
            description = "Updates volume quota, validity window, or notes. Modifiable only while status is ACTIVE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocation updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water allocation not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - allocation not in ACTIVE status")
    })
    public ResponseEntity<ApiResponse<WaterAllocationResponse>> updateWaterAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWaterAllocationRequest request
    ) {
        WaterAllocationResponse response = updateWaterAllocationUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/close")
    @Operation(
            summary = "Close a water allocation quota",
            description = "Transitions allocation status to CLOSED upon seasonal completion or natural expiration."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocation closed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water allocation not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - allocation not in ACTIVE status")
    })
    public ResponseEntity<ApiResponse<WaterAllocationResponse>> closeWaterAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelOperationRequest request
    ) {
        WaterAllocationResponse response = closeWaterAllocationUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel a water allocation quota",
            description = "Transitions allocation status to CANCELLED due to operational cuts or administrative revocation."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water allocation cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water allocation not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - allocation not in ACTIVE status")
    })
    public ResponseEntity<ApiResponse<WaterAllocationResponse>> cancelWaterAllocation(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelOperationRequest request
    ) {
        WaterAllocationResponse response = cancelWaterAllocationUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
