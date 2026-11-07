package com.aquaai.network.api;

import com.aquaai.network.api.dto.ActualDeliveryResponse;
import com.aquaai.network.api.dto.ApproveDispatchRequest;
import com.aquaai.network.api.dto.DispatchOptimizationRequest;
import com.aquaai.network.api.dto.DispatchScheduleResponse;
import com.aquaai.network.api.dto.RecordActualDeliveryRequest;
import com.aquaai.network.api.dto.RejectDispatchRequest;
import com.aquaai.network.application.usecase.ApproveDispatchScheduleUseCase;
import com.aquaai.network.application.usecase.ExecuteDispatchScheduleUseCase;
import com.aquaai.network.application.usecase.GetDispatchScheduleDetailUseCase;
import com.aquaai.network.application.usecase.ListActualDeliveriesUseCase;
import com.aquaai.network.application.usecase.ListDispatchSchedulesUseCase;
import com.aquaai.network.application.usecase.OptimizeCanalDispatchUseCase;
import com.aquaai.network.application.usecase.RecordActualDeliveryUseCase;
import com.aquaai.network.application.usecase.RejectDispatchScheduleUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Milestone 08.14: Canal Dispatch Optimization & Delivery Logging.
 * STRICT POLICY: Recommends, optimizes, and logs schedules, but DOES NOT autonomously actuate pumps or gates.
 * All real-world actions require authorized human approval.
 */
@RestController
@RequestMapping("/api/v1/water-network/dispatch")
@RequiredArgsConstructor
public class CanalDispatchController {

    private final OptimizeCanalDispatchUseCase optimizeCanalDispatchUseCase;
    private final ListDispatchSchedulesUseCase listDispatchSchedulesUseCase;
    private final GetDispatchScheduleDetailUseCase getDispatchScheduleDetailUseCase;
    private final ApproveDispatchScheduleUseCase approveDispatchScheduleUseCase;
    private final RejectDispatchScheduleUseCase rejectDispatchScheduleUseCase;
    private final ExecuteDispatchScheduleUseCase executeDispatchScheduleUseCase;
    private final RecordActualDeliveryUseCase recordActualDeliveryUseCase;
    private final ListActualDeliveriesUseCase listActualDeliveriesUseCase;

    @PostMapping("/optimize")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'AGRONOMIST')")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> optimizeDispatch(
            @Valid @RequestBody DispatchOptimizationRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(null);
        DispatchScheduleResponse response = optimizeCanalDispatchUseCase.execute(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<List<DispatchScheduleResponse>>> listSchedules(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID targetWuaId
    ) {
        List<DispatchScheduleResponse> response = listDispatchSchedulesUseCase.execute(status, targetWuaId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/schedules/{id}")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> getScheduleDetail(
            @PathVariable UUID id
    ) {
        DispatchScheduleResponse response = getDispatchScheduleDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/schedules/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> approveSchedule(
            @PathVariable UUID id,
            @RequestBody(required = false) ApproveDispatchRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        DispatchScheduleResponse response = approveDispatchScheduleUseCase.execute(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/schedules/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> rejectSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody RejectDispatchRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        DispatchScheduleResponse response = rejectDispatchScheduleUseCase.execute(id, request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/schedules/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> startExecution(
            @PathVariable UUID id
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        DispatchScheduleResponse response = executeDispatchScheduleUseCase.startExecution(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/schedules/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<DispatchScheduleResponse>> completeExecution(
            @PathVariable UUID id
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        DispatchScheduleResponse response = executeDispatchScheduleUseCase.completeExecution(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/actual-delivery")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<ActualDeliveryResponse>> recordActualDelivery(
            @Valid @RequestBody RecordActualDeliveryRequest request
    ) {
        UUID currentUserId = SecurityContextHelper.getCurrentPrincipal()
                .map(p -> p.getId())
                .orElse(UUID.randomUUID());
        ActualDeliveryResponse response = recordActualDeliveryUseCase.execute(request, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/actual-deliveries")
    public ResponseEntity<ApiResponse<List<ActualDeliveryResponse>>> listActualDeliveries(
            @RequestParam(required = false) UUID dispatchScheduleId,
            @RequestParam(required = false) UUID fieldId
    ) {
        List<ActualDeliveryResponse> response = listActualDeliveriesUseCase.execute(dispatchScheduleId, fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
