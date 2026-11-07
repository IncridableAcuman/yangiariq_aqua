package com.aquaai.risk.api;

import com.aquaai.risk.api.dto.AcknowledgeAssessmentRequest;
import com.aquaai.risk.api.dto.FarmRiskOverviewResponse;
import com.aquaai.risk.api.dto.FieldRiskProfileResponse;
import com.aquaai.risk.api.dto.ResolveAssessmentRequest;
import com.aquaai.risk.api.dto.RiskRecommendationResponse;
import com.aquaai.risk.application.usecase.AcknowledgeRiskAssessmentUseCase;
import com.aquaai.risk.application.usecase.GetFarmRiskOverviewUseCase;
import com.aquaai.risk.application.usecase.GetFieldRiskHistoryUseCase;
import com.aquaai.risk.application.usecase.GetFieldRiskProfileUseCase;
import com.aquaai.risk.application.usecase.GetRiskRecommendationDetailUseCase;
import com.aquaai.risk.application.usecase.ListFieldRiskProfilesUseCase;
import com.aquaai.risk.application.usecase.ListRiskRecommendationsUseCase;
import com.aquaai.risk.application.usecase.ResolveRiskAssessmentUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for Milestone 08.13: Farm-Level Predictive Risk & Resilience Intelligence.
 * Exposes explainable, evidence-based agricultural risk profiles and human-governed actions.
 */
@RestController
@RequestMapping("/api/v1/risk-intelligence")
@RequiredArgsConstructor
public class RiskIntelligenceController {

    private final GetFarmRiskOverviewUseCase getFarmRiskOverviewUseCase;
    private final ListFieldRiskProfilesUseCase listFieldRiskProfilesUseCase;
    private final GetFieldRiskProfileUseCase getFieldRiskProfileUseCase;
    private final GetFieldRiskHistoryUseCase getFieldRiskHistoryUseCase;
    private final ListRiskRecommendationsUseCase listRiskRecommendationsUseCase;
    private final GetRiskRecommendationDetailUseCase getRiskRecommendationDetailUseCase;
    private final AcknowledgeRiskAssessmentUseCase acknowledgeRiskAssessmentUseCase;
    private final ResolveRiskAssessmentUseCase resolveRiskAssessmentUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<FarmRiskOverviewResponse>> getOverview() {
        FarmRiskOverviewResponse response = getFarmRiskOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<PageResponse<FieldRiskProfileResponse>>> listFields(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String confidence,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "priority") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<FieldRiskProfileResponse> response = listFieldRiskProfilesUseCase.execute(
                severity, category, confidence, status, search, sortBy, sortDirection, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields/{fieldId}")
    public ResponseEntity<ApiResponse<FieldRiskProfileResponse>> getFieldProfile(
            @PathVariable UUID fieldId
    ) {
        FieldRiskProfileResponse response = getFieldRiskProfileUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/fields/{fieldId}/history")
    public ResponseEntity<ApiResponse<PageResponse<FieldRiskProfileResponse>>> getFieldHistory(
            @PathVariable UUID fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<FieldRiskProfileResponse> response = getFieldRiskHistoryUseCase.execute(
                fieldId, from, to, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<PageResponse<RiskRecommendationResponse>>> listRecommendations(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<RiskRecommendationResponse> response = listRiskRecommendationsUseCase.execute(
                severity, category, fieldId, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/recommendations/{id}")
    public ResponseEntity<ApiResponse<RiskRecommendationResponse>> getRecommendationDetail(
            @PathVariable UUID id
    ) {
        RiskRecommendationResponse response = getRiskRecommendationDetailUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/assessments/{id}/acknowledge")
    public ResponseEntity<ApiResponse<FieldRiskProfileResponse>> acknowledgeAssessment(
            @PathVariable UUID id,
            @RequestBody(required = false) AcknowledgeAssessmentRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        FieldRiskProfileResponse response = acknowledgeRiskAssessmentUseCase.execute(id, notes);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/assessments/{id}/resolve")
    public ResponseEntity<ApiResponse<FieldRiskProfileResponse>> resolveAssessment(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveAssessmentRequest request
    ) {
        FieldRiskProfileResponse response = resolveRiskAssessmentUseCase.execute(id, request.resolutionNotes());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
