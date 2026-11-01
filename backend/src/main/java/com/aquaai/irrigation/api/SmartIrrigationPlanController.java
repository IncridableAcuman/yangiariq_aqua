package com.aquaai.irrigation.api;

import com.aquaai.irrigation.api.dto.ApprovePlanRequest;
import com.aquaai.irrigation.api.dto.CancelPlanRequest;
import com.aquaai.irrigation.api.dto.CompletePlanRequest;
import com.aquaai.irrigation.api.dto.GeneratePlanRequest;
import com.aquaai.irrigation.api.dto.SmartIrrigationPlanResponse;
import com.aquaai.irrigation.application.usecase.ApproveSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.application.usecase.CancelSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.application.usecase.CompleteSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.application.usecase.GenerateSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.application.usecase.GetSmartIrrigationPlanUseCase;
import com.aquaai.irrigation.application.usecase.ListSmartIrrigationPlansUseCase;
import com.aquaai.irrigation.domain.model.IrrigationPlanStatus;
import com.aquaai.irrigation.domain.model.IrrigationPriority;
import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.irrigation.domain.port.SmartIrrigationPlanRepository;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.shared.domain.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmartIrrigationPlanController {

    private final GenerateSmartIrrigationPlanUseCase generatePlanUseCase;
    private final ApproveSmartIrrigationPlanUseCase approvePlanUseCase;
    private final CancelSmartIrrigationPlanUseCase cancelPlanUseCase;
    private final CompleteSmartIrrigationPlanUseCase completePlanUseCase;
    private final GetSmartIrrigationPlanUseCase getPlanUseCase;
    private final ListSmartIrrigationPlansUseCase listPlansUseCase;
    private final SmartIrrigationPlanRepository planRepository;

    @GetMapping("/irrigation/plans")
    public ResponseEntity<ApiResponse<PageResponse<SmartIrrigationPlanResponse>>> listPlans(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) IrrigationPlanStatus status,
            @RequestParam(required = false) IrrigationPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var paged = listPlansUseCase.execute(fieldId, status, priority, dateFrom, dateTo, page, size);
        List<SmartIrrigationPlanResponse> responses = paged.content().stream()
                .map(SmartIrrigationPlanResponse::fromDomain)
                .collect(Collectors.toList());

        int totalPages = paged.totalElements() > 0 ? (int) Math.ceil((double) paged.totalElements() / paged.size()) : 0;
        PageResponse<SmartIrrigationPlanResponse> pageResponse = PageResponse.<SmartIrrigationPlanResponse>builder()
                .content(responses)
                .page(paged.page())
                .size(paged.size())
                .totalElements(paged.totalElements())
                .totalPages(totalPages)
                .first(paged.page() == 0)
                .last(paged.page() >= totalPages - 1)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/irrigation/plans/{id}")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> getPlan(@PathVariable UUID id) {
        SmartIrrigationPlan plan = getPlanUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @GetMapping("/fields/{fieldId}/irrigation/plans")
    public ResponseEntity<ApiResponse<PageResponse<SmartIrrigationPlanResponse>>> listFieldPlans(
            @PathVariable UUID fieldId,
            @RequestParam(required = false) IrrigationPlanStatus status,
            @RequestParam(required = false) IrrigationPriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return listPlans(fieldId, status, priority, dateFrom, dateTo, page, size);
    }

    @GetMapping("/fields/{fieldId}/irrigation/plans/active")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> getActiveFieldPlan(@PathVariable UUID fieldId) {
        SmartIrrigationPlan plan = planRepository.findActiveByFieldId(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Active SmartIrrigationPlan", fieldId));
        // Verify ownership
        getPlanUseCase.execute(plan.getId());
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/fields/{fieldId}/irrigation/plans/generate")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> generatePlan(
            @PathVariable UUID fieldId,
            @RequestBody(required = false) GeneratePlanRequest request
    ) {
        LocalDate targetDate = request != null ? request.getTargetDate() : null;
        SmartIrrigationPlan plan = generatePlanUseCase.execute(fieldId, targetDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/irrigation/plans/{id}/approve")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> approvePlan(
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovePlanRequest request
    ) {
        boolean ack = request != null && request.isAcknowledgeWarnings();
        SmartIrrigationPlan plan = approvePlanUseCase.execute(id, ack);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/irrigation/plans/{id}/cancel")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> cancelPlan(
            @PathVariable UUID id,
            @Valid @RequestBody CancelPlanRequest request
    ) {
        SmartIrrigationPlan plan = cancelPlanUseCase.execute(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }

    @PostMapping("/irrigation/plans/{id}/complete")
    public ResponseEntity<ApiResponse<SmartIrrigationPlanResponse>> completePlan(
            @PathVariable UUID id,
            @RequestBody(required = false) CompletePlanRequest request
    ) {
        var actualVolume = request != null ? request.getActualVolumeDeliveredM3() : null;
        SmartIrrigationPlan plan = completePlanUseCase.execute(id, actualVolume);
        return ResponseEntity.ok(ApiResponse.ok(SmartIrrigationPlanResponse.fromDomain(plan)));
    }
}
