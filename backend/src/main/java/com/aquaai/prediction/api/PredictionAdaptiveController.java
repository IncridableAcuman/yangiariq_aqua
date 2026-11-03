package com.aquaai.prediction.api;

import com.aquaai.prediction.application.dto.AdaptiveOverviewResponse;
import com.aquaai.prediction.application.dto.AdaptiveSelectionDecisionDto;
import com.aquaai.prediction.application.dto.ContextPerformanceProfileDto;
import com.aquaai.prediction.application.dto.PredictionContextDto;
import com.aquaai.prediction.application.dto.SelectAdaptiveMethodRequest;
import com.aquaai.prediction.application.usecase.GetAdaptiveOverviewUseCase;
import com.aquaai.prediction.application.usecase.GetAdaptiveSelectionHistoryUseCase;
import com.aquaai.prediction.application.usecase.GetContextPerformanceProfilesUseCase;
import com.aquaai.prediction.application.usecase.GetFieldPredictionContextUseCase;
import com.aquaai.prediction.application.usecase.GetPredictionAdaptiveDecisionUseCase;
import com.aquaai.prediction.application.usecase.SelectAdaptiveMethodUseCase;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.prediction.domain.port.PredictionDataProvider;
import com.aquaai.prediction.domain.port.PredictionDataProvider.FieldMetadata;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.security.CurrentUserProvider;
import com.aquaai.shared.security.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
 * REST controller for Milestone 08.10: Context-Aware Adaptive Intelligence Engine.
 * <p>
 * Provides read and test endpoints for operational parcel context, contextual empirical
 * performance matrices, deterministic adaptive selection audit trail, and explainability.
 */
@RestController
@RequestMapping("/api/v1/predictions")
@RequiredArgsConstructor
public class PredictionAdaptiveController {

    private final GetAdaptiveOverviewUseCase getAdaptiveOverviewUseCase;
    private final GetFieldPredictionContextUseCase getFieldPredictionContextUseCase;
    private final GetContextPerformanceProfilesUseCase getContextPerformanceProfilesUseCase;
    private final GetAdaptiveSelectionHistoryUseCase getAdaptiveSelectionHistoryUseCase;
    private final SelectAdaptiveMethodUseCase selectAdaptiveMethodUseCase;
    private final GetPredictionAdaptiveDecisionUseCase getPredictionAdaptiveDecisionUseCase;
    private final PredictionDataProvider dataProvider;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/adaptive/overview")
    public ResponseEntity<ApiResponse<AdaptiveOverviewResponse>> getOverview() {
        AdaptiveOverviewResponse response = getAdaptiveOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/adaptive/context/{fieldId}")
    public ResponseEntity<ApiResponse<PredictionContextDto>> getFieldContext(@PathVariable UUID fieldId) {
        verifyFieldAccess(fieldId);
        PredictionContextDto response = getFieldPredictionContextUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/adaptive/performance")
    public ResponseEntity<ApiResponse<List<ContextPerformanceProfileDto>>> getPerformanceProfiles(
            @RequestParam(required = false) String predictionType,
            @RequestParam(required = false) String versionTag
    ) {
        PredictionType type = null;
        if (predictionType != null && !predictionType.isBlank()) {
            try {
                type = PredictionType.valueOf(predictionType.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        List<ContextPerformanceProfileDto> response = getContextPerformanceProfilesUseCase.execute(type, versionTag);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/adaptive/selections")
    public ResponseEntity<ApiResponse<List<AdaptiveSelectionDecisionDto>>> getSelectionHistory(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false, defaultValue = "20") int limit
    ) {
        if (fieldId != null) {
            verifyFieldAccess(fieldId);
        }
        List<AdaptiveSelectionDecisionDto> response = getAdaptiveSelectionHistoryUseCase.execute(fieldId, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/adaptive/select")
    public ResponseEntity<ApiResponse<AdaptiveSelectionDecisionDto>> selectAdaptiveMethod(
            @RequestBody SelectAdaptiveMethodRequest request
    ) {
        if (request == null || request.fieldId() == null) {
            throw new IllegalArgumentException("fieldId is required");
        }
        verifyFieldAccess(request.fieldId());

        PredictionType type = PredictionType.SOIL_MOISTURE_FORECAST;
        if (request.predictionType() != null && !request.predictionType().isBlank()) {
            try {
                type = PredictionType.valueOf(request.predictionType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid predictionType: " + request.predictionType());
            }
        }

        AdaptiveSelectionDecisionDto decision = selectAdaptiveMethodUseCase.execute(request.fieldId(), type);
        return ResponseEntity.ok(ApiResponse.ok(decision));
    }

    @GetMapping("/{id}/adaptive-decision")
    public ResponseEntity<ApiResponse<AdaptiveSelectionDecisionDto>> getDecisionByPrediction(@PathVariable UUID id) {
        AdaptiveSelectionDecisionDto decision = getPredictionAdaptiveDecisionUseCase.execute(id)
                .orElseThrow(() -> new ResourceNotFoundException("AdaptiveSelectionDecision", id));
        verifyFieldAccess(decision.fieldId());
        return ResponseEntity.ok(ApiResponse.ok(decision));
    }

    private void verifyFieldAccess(UUID fieldId) {
        UserRole currentRole = currentUserProvider.getRequiredUserRole();
        UUID currentUserId = currentUserProvider.getRequiredUserId();

        if (currentRole == UserRole.FARMER) {
            FieldMetadata field = dataProvider.getFieldMetadata(fieldId)
                    .orElseThrow(() -> new ResourceNotFoundException("Field", fieldId));
            if (field.ownerId() == null || !field.ownerId().equals(currentUserId)) {
                throw new ResourceNotFoundException("Field", fieldId);
            }
        }
    }
}
