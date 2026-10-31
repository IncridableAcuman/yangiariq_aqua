package com.aquaai.water.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.water.api.dto.CancelOperationRequest;
import com.aquaai.water.api.dto.CompleteIrrigationEventRequest;
import com.aquaai.water.api.dto.CreateIrrigationEventRequest;
import com.aquaai.water.api.dto.IrrigationEventResponse;
import com.aquaai.water.api.dto.RecordCompletedIrrigationEventRequest;
import com.aquaai.water.application.usecase.CancelIrrigationEventUseCase;
import com.aquaai.water.application.usecase.CompleteIrrigationEventUseCase;
import com.aquaai.water.application.usecase.GetIrrigationEventUseCase;
import com.aquaai.water.application.usecase.ListIrrigationEventsUseCase;
import com.aquaai.water.application.usecase.RecordCompletedIrrigationEventUseCase;
import com.aquaai.water.application.usecase.StartIrrigationEventUseCase;
import com.aquaai.water.domain.model.IrrigationEventStatus;
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
 * REST controller for executed watering activities, physical consumption recording, and operational events.
 * Enforces parcel ownership, plan consistency, and completion invariants.
 */
@RestController
@RequestMapping("/api/v1/irrigation-events")
@RequiredArgsConstructor
@Tag(name = "Irrigation Events & Water Consumption", description = "Endpoints for logging executed watering operations and recording actual water volume consumption")
@SecurityRequirement(name = "bearerAuth")
public class IrrigationEventController {

    private final StartIrrigationEventUseCase startIrrigationEventUseCase;
    private final RecordCompletedIrrigationEventUseCase recordCompletedIrrigationEventUseCase;
    private final CompleteIrrigationEventUseCase completeIrrigationEventUseCase;
    private final CancelIrrigationEventUseCase cancelIrrigationEventUseCase;
    private final GetIrrigationEventUseCase getIrrigationEventUseCase;
    private final ListIrrigationEventsUseCase listIrrigationEventsUseCase;

    @PostMapping
    @Operation(
            summary = "Start a real-time irrigation event operation",
            description = "Initiates an in-progress watering event. Validates field ownership and scheduled plan consistency."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Irrigation event started successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field or plan not found (anti-enumeration for unowned fields)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - plan belongs to different field")
    })
    public ResponseEntity<ApiResponse<IrrigationEventResponse>> startIrrigationEvent(
            @Valid @RequestBody CreateIrrigationEventRequest request
    ) {
        IrrigationEventResponse response = startIrrigationEventUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/completed")
    @Operation(
            summary = "Directly record an already-completed irrigation delivery",
            description = "Logs historical or completed irrigation with execution timestamps and consumed water volume (m³)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Completed irrigation event recorded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field or plan not found (anti-enumeration for unowned fields)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - plan mismatch or negative volume")
    })
    public ResponseEntity<ApiResponse<IrrigationEventResponse>> recordCompletedIrrigationEvent(
            @Valid @RequestBody RecordCompletedIrrigationEventRequest request
    ) {
        IrrigationEventResponse response = recordCompletedIrrigationEventUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get irrigation event details by ID",
            description = "Retrieves execution event details. Enforces ownership: Farmers access only events on parcels they own (404 anti-enumeration)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation event retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation event not found or not accessible")
    })
    public ResponseEntity<ApiResponse<IrrigationEventResponse>> getIrrigationEventById(
            @PathVariable UUID id
    ) {
        IrrigationEventResponse response = getIrrigationEventUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(
            summary = "List irrigation events with filtering and pagination",
            description = "For FARMER, lists events strictly for owned parcels. For OPERATOR and ADMIN, lists events district-wide with optional field filtering."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation events retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Specified field not found or unowned")
    })
    public ResponseEntity<ApiResponse<PageResponse<IrrigationEventResponse>>> listIrrigationEvents(
            @Parameter(description = "Filter by parcel field ID")
            @RequestParam(required = false) UUID fieldId,

            @Parameter(description = "Filter by scheduled irrigation plan ID")
            @RequestParam(required = false) UUID irrigationPlanId,

            @Parameter(description = "Filter by execution status")
            @RequestParam(required = false) IrrigationEventStatus status,

            @Parameter(description = "Filter execution window start timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,

            @Parameter(description = "Filter execution window end timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort property", example = "startedAt")
            @RequestParam(defaultValue = "startedAt") String sortBy,

            @Parameter(description = "Sort direction ('asc' or 'desc')", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResponse<IrrigationEventResponse> response = listIrrigationEventsUseCase.execute(
                fieldId,
                irrigationPlanId,
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

    @PatchMapping("/{id}/complete")
    @Operation(
            summary = "Complete an in-progress irrigation event",
            description = "Transitions event to COMPLETED by recording endedAt timestamp and physical consumed volume (m³)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation event completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation event not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - event not in STARTED status or invalid volume")
    })
    public ResponseEntity<ApiResponse<IrrigationEventResponse>> completeIrrigationEvent(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteIrrigationEventRequest request
    ) {
        IrrigationEventResponse response = completeIrrigationEventUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel an in-progress irrigation delivery operation",
            description = "Transitions event from STARTED to CANCELLED with optional operational reason notes."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Irrigation event cancelled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Irrigation event not found or not accessible"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - cannot cancel already completed event")
    })
    public ResponseEntity<ApiResponse<IrrigationEventResponse>> cancelIrrigationEvent(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelOperationRequest request
    ) {
        IrrigationEventResponse response = cancelIrrigationEventUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
