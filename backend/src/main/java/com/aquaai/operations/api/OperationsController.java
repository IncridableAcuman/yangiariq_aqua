package com.aquaai.operations.api;

import com.aquaai.irrigation.api.dto.SmartIrrigationPlanResponse;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.operations.api.dto.ApproveOperationalPlanRequest;
import com.aquaai.operations.api.dto.CompleteOperationRequest;
import com.aquaai.operations.api.dto.OperationalContextResponse;
import com.aquaai.operations.api.dto.OperationalVerificationResponse;
import com.aquaai.operations.api.dto.PreparePlanRequest;
import com.aquaai.operations.api.dto.RejectOperationalPlanRequest;
import com.aquaai.operations.api.dto.ScheduleOperationalPlanRequest;
import com.aquaai.operations.api.dto.StartOperationRequest;
import com.aquaai.operations.application.usecase.ApproveOperationalPlanUseCase;
import com.aquaai.operations.application.usecase.CompleteOperationalExecutionUseCase;
import com.aquaai.operations.application.usecase.GetOperationalContextUseCase;
import com.aquaai.operations.application.usecase.GetOperationalVerificationUseCase;
import com.aquaai.operations.application.usecase.PrepareOperationalPlanUseCase;
import com.aquaai.operations.application.usecase.RejectOperationalPlanUseCase;
import com.aquaai.operations.application.usecase.ScheduleOperationalPlanUseCase;
import com.aquaai.operations.application.usecase.StartOperationalExecutionUseCase;
import com.aquaai.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller orchestrating the end-to-end Decision-to-Action irrigation operations workflow.
 */
@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final GetOperationalContextUseCase getOperationalContextUseCase;
    private final PrepareOperationalPlanUseCase prepareOperationalPlanUseCase;
    private final ApproveOperationalPlanUseCase approveOperationalPlanUseCase;
    private final RejectOperationalPlanUseCase rejectOperationalPlanUseCase;
    private final ScheduleOperationalPlanUseCase scheduleOperationalPlanUseCase;
    private final StartOperationalExecutionUseCase startOperationalExecutionUseCase;
    private final CompleteOperationalExecutionUseCase completeOperationalExecutionUseCase;
    private final GetOperationalVerificationUseCase getOperationalVerificationUseCase;

    @GetMapping("/{fieldId}/context")
    public ResponseEntity<ApiResponse<OperationalContextResponse>> getContext(@PathVariable UUID fieldId) {
        OperationalContextResponse response = getOperationalContextUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{fieldId}/prepare-plan")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> preparePlan(
            @PathVariable UUID fieldId,
            @RequestBody(required = false) PreparePlanRequest request
    ) {
        SmartIrrigationPlan plan = prepareOperationalPlanUseCase.execute(fieldId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/plans/{planId}/approve")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> approvePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody ApproveOperationalPlanRequest request
    ) {
        SmartIrrigationPlan plan = approveOperationalPlanUseCase.execute(planId, request);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/plans/{planId}/reject")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> rejectPlan(
            @PathVariable UUID planId,
            @Valid @RequestBody RejectOperationalPlanRequest request
    ) {
        SmartIrrigationPlan plan = rejectOperationalPlanUseCase.execute(planId, request);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/plans/{planId}/schedule")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> schedulePlan(
            @PathVariable UUID planId,
            @RequestBody(required = false) ScheduleOperationalPlanRequest request
    ) {
        SmartIrrigationPlan plan = scheduleOperationalPlanUseCase.execute(planId, request);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/plans/{planId}/start")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> startOperation(
            @PathVariable UUID planId,
            @RequestBody(required = false) StartOperationRequest request
    ) {
        SmartIrrigationPlan plan = startOperationalExecutionUseCase.execute(planId, request);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/plans/{planId}/complete")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> completeOperation(
            @PathVariable UUID planId,
            @Valid @RequestBody(required = false) CompleteOperationRequest request
    ) {
        SmartIrrigationPlan plan = completeOperationalExecutionUseCase.execute(planId, request);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @GetMapping("/plans/{planId}/verification")
    public ResponseEntity<ApiResponse<OperationalVerificationResponse>> getVerification(@PathVariable UUID planId) {
        OperationalVerificationResponse response = getOperationalVerificationUseCase.execute(planId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
