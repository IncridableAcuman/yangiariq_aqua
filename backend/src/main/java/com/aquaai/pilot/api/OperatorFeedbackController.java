package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.OperatorFeedbackResponse;
import com.aquaai.pilot.application.dto.PilotDtos.SubmitFeedbackRequest;
import com.aquaai.pilot.application.dto.PilotDtos.UpdateFeedbackStatusRequest;
import com.aquaai.pilot.application.usecase.OperatorFeedbackUseCase;
import com.aquaai.pilot.domain.model.OperatorFeedbackCategory;
import com.aquaai.pilot.domain.model.OperatorFeedbackStatus;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pilot-feedback")
@Tag(name = "Operator Field Observations & Feedback", description = "Endpoints for recording and investigating field feedback and operational discrepancies")
@SecurityRequirement(name = "bearerAuth")
public class OperatorFeedbackController {

    private final OperatorFeedbackUseCase feedbackUseCase;

    public OperatorFeedbackController(OperatorFeedbackUseCase feedbackUseCase) {
        this.feedbackUseCase = feedbackUseCase;
    }

    @GetMapping("/pilot/{pilotId}")
    @Operation(summary = "List operator feedback", description = "Returns feedback entries for a pilot, optionally filtered by status or category")
    public ResponseEntity<ApiResponse<List<OperatorFeedbackResponse>>> listFeedback(
            @PathVariable UUID pilotId,
            @RequestParam(required = false) OperatorFeedbackStatus status,
            @RequestParam(required = false) OperatorFeedbackCategory category
    ) {
        List<OperatorFeedbackResponse> list = feedbackUseCase.listFeedback(pilotId, status, category);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/pilot/{pilotId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST', 'FARMER')")
    @Operation(summary = "Submit field feedback", description = "Logs a qualitative field observation, sensor issue, or discrepancy")
    public ResponseEntity<ApiResponse<OperatorFeedbackResponse>> submitFeedback(
            @PathVariable UUID pilotId,
            @RequestBody SubmitFeedbackRequest request
    ) {
        UUID submitterId = SecurityContextHelper.getCurrentUserId();
        OperatorFeedbackResponse response = feedbackUseCase.submitFeedback(pilotId, request, submitterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/{feedbackId}/status")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST')")
    @Operation(summary = "Update feedback status", description = "Updates feedback lifecycle status and assigns responsible investigator")
    public ResponseEntity<ApiResponse<OperatorFeedbackResponse>> updateStatus(
            @PathVariable UUID feedbackId,
            @RequestBody UpdateFeedbackStatusRequest request
    ) {
        OperatorFeedbackResponse response = feedbackUseCase.updateStatus(feedbackId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback details", description = "Retrieves full details of a specific feedback record")
    public ResponseEntity<ApiResponse<OperatorFeedbackResponse>> getFeedback(@PathVariable UUID id) {
        OperatorFeedbackResponse response = feedbackUseCase.getFeedback(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
