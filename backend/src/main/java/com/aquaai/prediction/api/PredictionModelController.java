package com.aquaai.prediction.api;

import com.aquaai.prediction.application.dto.ActiveModelComparisonResponse;
import com.aquaai.prediction.application.dto.ModelCandidateDetailResponse;
import com.aquaai.prediction.application.dto.ModelCandidateDto;
import com.aquaai.prediction.application.dto.PromoteCandidateRequest;
import com.aquaai.prediction.application.dto.TrainPredictionModelRequest;
import com.aquaai.prediction.application.dto.TrainPredictionModelResponse;
import com.aquaai.prediction.application.dto.TrainingDatasetDetailResponse;
import com.aquaai.prediction.application.dto.TrainingDatasetDto;
import com.aquaai.prediction.application.usecase.GetActiveModelComparisonUseCase;
import com.aquaai.prediction.application.usecase.GetModelCandidateDetailUseCase;
import com.aquaai.prediction.application.usecase.GetModelCandidatesUseCase;
import com.aquaai.prediction.application.usecase.GetTrainingDatasetDetailUseCase;
import com.aquaai.prediction.application.usecase.GetTrainingDatasetsUseCase;
import com.aquaai.prediction.application.usecase.PromoteModelCandidateUseCase;
import com.aquaai.prediction.application.usecase.TrainPredictionModelUseCase;
import com.aquaai.prediction.domain.model.PredictionModelCandidate;
import com.aquaai.prediction.domain.model.PredictionType;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.shared.domain.UnauthorizedOperationException;
import com.aquaai.shared.security.SecurityContextHelper;
import com.aquaai.shared.security.UserPrincipal;
import com.aquaai.shared.security.UserRole;
import jakarta.validation.Valid;
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
 * REST controller for Milestone 08.11: Hybrid ML Prediction Engine & Evidence-Based Learning.
 * Provides endpoints for controlled training, reproducible dataset inspection,
 * candidate evaluation, baseline comparisons, and governed promotions.
 */
@RestController
@RequestMapping("/api/v1/predictions/models")
@RequiredArgsConstructor
public class PredictionModelController {

    private final TrainPredictionModelUseCase trainPredictionModelUseCase;
    private final GetModelCandidatesUseCase getModelCandidatesUseCase;
    private final GetModelCandidateDetailUseCase getModelCandidateDetailUseCase;
    private final GetTrainingDatasetsUseCase getTrainingDatasetsUseCase;
    private final GetTrainingDatasetDetailUseCase getTrainingDatasetDetailUseCase;
    private final PromoteModelCandidateUseCase promoteModelCandidateUseCase;
    private final GetActiveModelComparisonUseCase getActiveModelComparisonUseCase;

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<ModelCandidateDto>>> getCandidates(
            @RequestParam(required = false) String type
    ) {
        PredictionType pType = parsePredictionType(type);
        List<ModelCandidateDto> candidates = getModelCandidatesUseCase.execute(pType);
        return ResponseEntity.ok(ApiResponse.ok(candidates));
    }

    @GetMapping("/candidates/{id}")
    public ResponseEntity<ApiResponse<ModelCandidateDetailResponse>> getCandidateDetail(
            @PathVariable UUID id
    ) {
        ModelCandidateDetailResponse detail = getModelCandidateDetailUseCase.execute(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model candidate not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @GetMapping("/datasets")
    public ResponseEntity<ApiResponse<List<TrainingDatasetDto>>> getDatasets(
            @RequestParam(required = false) String type
    ) {
        PredictionType pType = parsePredictionType(type);
        List<TrainingDatasetDto> datasets = getTrainingDatasetsUseCase.execute(pType);
        return ResponseEntity.ok(ApiResponse.ok(datasets));
    }

    @GetMapping("/datasets/{id}")
    public ResponseEntity<ApiResponse<TrainingDatasetDetailResponse>> getDatasetDetail(
            @PathVariable UUID id
    ) {
        TrainingDatasetDetailResponse detail = getTrainingDatasetDetailUseCase.execute(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training dataset snapshot not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(detail));
    }

    @PostMapping("/train")
    public ResponseEntity<ApiResponse<TrainPredictionModelResponse>> trainModel(
            @Valid @RequestBody TrainPredictionModelRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        TrainPredictionModelResponse response = trainPredictionModelUseCase.execute(
                request,
                principal.getUsername(),
                principal.getRole().name()
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/candidates/{id}/promote")
    public ResponseEntity<ApiResponse<ModelCandidateDetailResponse>> promoteCandidate(
            @PathVariable UUID id,
            @Valid @RequestBody PromoteCandidateRequest request
    ) {
        enforceAdminRole();
        UserPrincipal principal = SecurityContextHelper.getRequiredPrincipal();
        PredictionModelCandidate updated = promoteModelCandidateUseCase.execute(
                id,
                request,
                principal.getUsername(),
                principal.getRole().name()
        );
        ModelCandidateDetailResponse response = getModelCandidateDetailUseCase.execute(updated.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Model candidate not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/comparison")
    public ResponseEntity<ApiResponse<ActiveModelComparisonResponse>> getActiveComparison(
            @RequestParam(required = false) String type
    ) {
        PredictionType pType = parsePredictionType(type);
        ActiveModelComparisonResponse response = getActiveModelComparisonUseCase.execute(pType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private void enforceAdminRole() {
        if (!SecurityContextHelper.hasRole(UserRole.ADMIN)) {
            throw new UnauthorizedOperationException("Only system administrators can train or promote ML model candidates.");
        }
    }

    private PredictionType parsePredictionType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return PredictionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
