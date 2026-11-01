package com.aquaai.recommendation.api;

import com.aquaai.recommendation.api.dto.DismissRecommendationRequest;
import com.aquaai.recommendation.api.dto.RecommendationResponse;
import com.aquaai.recommendation.api.dto.RecommendationSummaryResponse;
import com.aquaai.recommendation.application.usecase.AcceptRecommendationUseCase;
import com.aquaai.recommendation.application.usecase.CompleteRecommendationUseCase;
import com.aquaai.recommendation.application.usecase.DismissRecommendationUseCase;
import com.aquaai.recommendation.application.usecase.GenerateRecommendationUseCase;
import com.aquaai.recommendation.application.usecase.GetRecommendationSummaryUseCase;
import com.aquaai.recommendation.application.usecase.GetRecommendationUseCase;
import com.aquaai.recommendation.application.usecase.ListRecommendationsUseCase;
import com.aquaai.recommendation.application.usecase.ViewRecommendationUseCase;
import com.aquaai.recommendation.domain.model.AgronomicRecommendation;
import com.aquaai.recommendation.domain.model.RecommendationPriority;
import com.aquaai.recommendation.domain.model.RecommendationStatus;
import com.aquaai.recommendation.domain.model.RecommendationSummaryData;
import com.aquaai.recommendation.domain.model.RecommendationType;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final GetRecommendationUseCase getRecommendationUseCase;
    private final ListRecommendationsUseCase listRecommendationsUseCase;
    private final GetRecommendationSummaryUseCase getRecommendationSummaryUseCase;
    private final ViewRecommendationUseCase viewRecommendationUseCase;
    private final AcceptRecommendationUseCase acceptRecommendationUseCase;
    private final DismissRecommendationUseCase dismissRecommendationUseCase;
    private final CompleteRecommendationUseCase completeRecommendationUseCase;

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> listRecommendations(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) RecommendationStatus status,
            @RequestParam(required = false) RecommendationPriority priority,
            @RequestParam(required = false) RecommendationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListRecommendationsUseCase.Result result = listRecommendationsUseCase.execute(new ListRecommendationsUseCase.Query(
                fieldId, status, priority, type, from, to, page, size
        ));

        List<RecommendationResponse> responses = result.content().stream()
                .map(RecommendationResponse::fromDomain)
                .toList();

        int totalPages = result.totalElements() > 0 ? (int) Math.ceil((double) result.totalElements() / result.size()) : 0;

        PageResponse<RecommendationResponse> pageResponse = PageResponse.<RecommendationResponse>builder()
                .content(responses)
                .page(result.page())
                .size(result.size())
                .totalElements(result.totalElements())
                .totalPages(totalPages)
                .first(result.page() == 0)
                .last(result.page() >= totalPages - 1)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @GetMapping("/recommendations/{id}")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getRecommendation(@PathVariable UUID id) {
        AgronomicRecommendation rec = getRecommendationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PostMapping("/recommendations/generate")
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateRecommendation(@RequestParam UUID fieldId) {
        AgronomicRecommendation rec = generateRecommendationUseCase.execute(fieldId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @GetMapping("/fields/{fieldId}/recommendations")
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> listFieldRecommendations(
            @PathVariable UUID fieldId,
            @RequestParam(required = false) RecommendationStatus status,
            @RequestParam(required = false) RecommendationPriority priority,
            @RequestParam(required = false) RecommendationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListRecommendationsUseCase.Result result = listRecommendationsUseCase.execute(new ListRecommendationsUseCase.Query(
                fieldId, status, priority, type, null, null, page, size
        ));

        List<RecommendationResponse> responses = result.content().stream()
                .map(RecommendationResponse::fromDomain)
                .toList();

        int totalPages = result.totalElements() > 0 ? (int) Math.ceil((double) result.totalElements() / result.size()) : 0;

        PageResponse<RecommendationResponse> pageResponse = PageResponse.<RecommendationResponse>builder()
                .content(responses)
                .page(result.page())
                .size(result.size())
                .totalElements(result.totalElements())
                .totalPages(totalPages)
                .first(result.page() == 0)
                .last(result.page() >= totalPages - 1)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }

    @PostMapping("/fields/{fieldId}/recommendations/generate")
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateFieldRecommendation(@PathVariable UUID fieldId) {
        AgronomicRecommendation rec = generateRecommendationUseCase.execute(fieldId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PostMapping("/fields/{fieldId}/recommendations/evaluate")
    public ResponseEntity<ApiResponse<RecommendationResponse>> evaluateFieldRecommendation(@PathVariable UUID fieldId) {
        AgronomicRecommendation rec = generateRecommendationUseCase.execute(fieldId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PatchMapping("/recommendations/{id}/view")
    public ResponseEntity<ApiResponse<RecommendationResponse>> view(@PathVariable UUID id) {
        AgronomicRecommendation rec = viewRecommendationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PatchMapping("/recommendations/{id}/accept")
    public ResponseEntity<ApiResponse<RecommendationResponse>> accept(@PathVariable UUID id) {
        AgronomicRecommendation rec = acceptRecommendationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PatchMapping("/recommendations/{id}/dismiss")
    public ResponseEntity<ApiResponse<RecommendationResponse>> dismiss(
            @PathVariable UUID id,
            @Valid @RequestBody DismissRecommendationRequest request
    ) {
        AgronomicRecommendation rec = dismissRecommendationUseCase.execute(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @PatchMapping("/recommendations/{id}/complete")
    public ResponseEntity<ApiResponse<RecommendationResponse>> complete(@PathVariable UUID id) {
        AgronomicRecommendation rec = completeRecommendationUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationResponse.fromDomain(rec)));
    }

    @GetMapping("/recommendations/summary")
    public ResponseEntity<ApiResponse<RecommendationSummaryResponse>> getSummary(@RequestParam(required = false) UUID fieldId) {
        RecommendationSummaryData data = getRecommendationSummaryUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationSummaryResponse.fromDomain(data)));
    }

    @GetMapping("/fields/{fieldId}/recommendations/summary")
    public ResponseEntity<ApiResponse<RecommendationSummaryResponse>> getFieldSummary(@PathVariable UUID fieldId) {
        RecommendationSummaryData data = getRecommendationSummaryUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(RecommendationSummaryResponse.fromDomain(data)));
    }
}
