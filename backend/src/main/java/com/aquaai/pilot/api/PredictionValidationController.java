package com.aquaai.pilot.api;

import com.aquaai.pilot.application.dto.PilotDtos.EvaluatePredictionRequest;
import com.aquaai.pilot.application.dto.PilotDtos.PredictionValidationResponse;
import com.aquaai.pilot.application.usecase.PredictionValidationUseCase;
import com.aquaai.pilot.domain.model.PredictionEvaluationStatus;
import com.aquaai.shared.api.ApiResponse;
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
@RequestMapping("/api/v1/pilot-validation")
@Tag(name = "Prediction vs Actual Validation", description = "Endpoints for comparing predictive model forecasts against verified ground-truth observations")
@SecurityRequirement(name = "bearerAuth")
public class PredictionValidationController {

    private final PredictionValidationUseCase validationUseCase;

    public PredictionValidationController(PredictionValidationUseCase validationUseCase) {
        this.validationUseCase = validationUseCase;
    }

    @GetMapping("/pilot/{pilotId}")
    @Operation(summary = "List prediction validation records", description = "Returns validation records comparing model forecasts to verified measurements")
    public ResponseEntity<ApiResponse<List<PredictionValidationResponse>>> listValidations(
            @PathVariable UUID pilotId,
            @RequestParam(required = false) PredictionEvaluationStatus status
    ) {
        List<PredictionValidationResponse> list = validationUseCase.listValidations(pilotId, status);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/pilot/{pilotId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'AGRONOMIST')")
    @Operation(summary = "Evaluate prediction vs actual", description = "Matches a model forecast with verified actual observation, calculating error metrics")
    public ResponseEntity<ApiResponse<PredictionValidationResponse>> evaluatePrediction(
            @PathVariable UUID pilotId,
            @RequestBody EvaluatePredictionRequest request
    ) {
        PredictionValidationResponse response = validationUseCase.evaluatePrediction(pilotId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get prediction validation details", description = "Retrieves error metrics and model provenance for a specific comparison")
    public ResponseEntity<ApiResponse<PredictionValidationResponse>> getValidation(@PathVariable UUID id) {
        PredictionValidationResponse response = validationUseCase.getValidation(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
