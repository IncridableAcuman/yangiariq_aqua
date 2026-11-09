package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.GenerateEvaluationReportRequest;
import com.aquaai.pilot.application.dto.PilotDtos.PilotEvaluationReportResponse;
import com.aquaai.pilot.application.dto.PilotDtos.PilotOperationalMonitoringResponse;
import com.aquaai.pilot.application.usecase.PilotMonitoringAndEvaluationUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.security.SecurityContextHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pilot-evaluations")
@Tag(name = "Pilot Operational Monitoring & Evaluation", description = "Endpoints for real-time pilot monitoring and defensible evidence-based evaluation reporting")
@SecurityRequirement(name = "bearerAuth")
public class PilotEvaluationController {

    private final PilotMonitoringAndEvaluationUseCase monitoringAndEvaluationUseCase;

    public PilotEvaluationController(PilotMonitoringAndEvaluationUseCase monitoringAndEvaluationUseCase) {
        this.monitoringAndEvaluationUseCase = monitoringAndEvaluationUseCase;
    }

    @GetMapping("/pilot/{pilotId}/monitoring")
    @Operation(summary = "Get pilot operational monitoring KPIs", description = "Returns real-time operational indicators, measurement frequency, and verification backlog")
    public ResponseEntity<ApiResponse<PilotOperationalMonitoringResponse>> getOperationalMonitoring(@PathVariable UUID pilotId) {
        PilotOperationalMonitoringResponse response = monitoringAndEvaluationUseCase.getOperationalMonitoring(pilotId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/pilot/{pilotId}/report")
    @Operation(summary = "Get latest pilot evaluation report", description = "Retrieves the most recent evidence-backed evaluation snapshot")
    public ResponseEntity<ApiResponse<PilotEvaluationReportResponse>> getLatestEvaluationReport(@PathVariable UUID pilotId) {
        PilotEvaluationReportResponse response = monitoringAndEvaluationUseCase.getLatestEvaluationReport(pilotId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/pilot/{pilotId}/report")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Generate pilot evaluation report snapshot", description = "Synthesizes data quality, prediction accuracy, operational response, and limitations into an immutable report snapshot")
    public ResponseEntity<ApiResponse<PilotEvaluationReportResponse>> generateEvaluationReport(
            @PathVariable UUID pilotId,
            @RequestBody GenerateEvaluationReportRequest request
    ) {
        UUID creatorId = SecurityContextHelper.getCurrentUserId();
        PilotEvaluationReportResponse response = monitoringAndEvaluationUseCase.generateEvaluationReport(pilotId, request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }
}
