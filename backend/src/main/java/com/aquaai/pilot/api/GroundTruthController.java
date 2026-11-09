package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.GroundTruthResponse;
import com.aquaai.pilot.application.dto.PilotDtos.SubmitGroundTruthRequest;
import com.aquaai.pilot.application.dto.PilotDtos.VerifyGroundTruthRequest;
import com.aquaai.pilot.application.usecase.GroundTruthUseCase;
import com.aquaai.pilot.domain.model.GroundTruthStatus;
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
@RequestMapping("/api/v1/pilot-ground-truth")
@Tag(name = "Ground-Truth Validation", description = "Endpoints for collecting and four-eyes independent verification of physical field measurements")
@SecurityRequirement(name = "bearerAuth")
public class GroundTruthController {

    private final GroundTruthUseCase groundTruthUseCase;

    public GroundTruthController(GroundTruthUseCase groundTruthUseCase) {
        this.groundTruthUseCase = groundTruthUseCase;
    }

    @GetMapping("/pilot/{pilotId}")
    @Operation(summary = "List ground-truth measurements", description = "Returns ground-truth records for a pilot, optionally filtered by status")
    public ResponseEntity<ApiResponse<List<GroundTruthResponse>>> listMeasurements(
            @PathVariable UUID pilotId,
            @RequestParam(required = false) GroundTruthStatus status
    ) {
        List<GroundTruthResponse> list = groundTruthUseCase.listMeasurements(pilotId, status);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/pilot/{pilotId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST', 'FARMER')")
    @Operation(summary = "Submit ground-truth measurement", description = "Submits a physical field measurement for review and verification")
    public ResponseEntity<ApiResponse<GroundTruthResponse>> submitMeasurement(
            @PathVariable UUID pilotId,
            @RequestBody SubmitGroundTruthRequest request
    ) {
        UUID submitterId = SecurityContextHelper.getCurrentUserId();
        GroundTruthResponse response = groundTruthUseCase.submitMeasurement(pilotId, request, submitterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/{measurementId}/review")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST')")
    @Operation(summary = "Review and verify ground truth", description = "Independently verifies, rejects, or requests remeasurement under the four-eyes principle")
    public ResponseEntity<ApiResponse<GroundTruthResponse>> reviewMeasurement(
            @PathVariable UUID measurementId,
            @RequestBody VerifyGroundTruthRequest request
    ) {
        UUID verifierId = SecurityContextHelper.getCurrentUserId();
        GroundTruthResponse response = groundTruthUseCase.reviewMeasurement(measurementId, request, verifierId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{measurementId}")
    @Operation(summary = "Get ground truth details", description = "Retrieves full details and provenance for a single measurement")
    public ResponseEntity<ApiResponse<GroundTruthResponse>> getMeasurement(@PathVariable UUID measurementId) {
        GroundTruthResponse response = groundTruthUseCase.getMeasurement(measurementId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
