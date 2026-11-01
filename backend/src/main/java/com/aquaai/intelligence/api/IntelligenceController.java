package com.aquaai.intelligence.api;

import com.aquaai.intelligence.application.dto.AgronomicAssessmentResponse;
import com.aquaai.intelligence.application.dto.IntelligenceEvaluationResponse;
import com.aquaai.intelligence.application.dto.TelemetrySignalResponse;
import com.aquaai.intelligence.application.usecase.EvaluateFieldIntelligenceUseCase;
import com.aquaai.intelligence.application.usecase.GetFieldAgronomicAssessmentUseCase;
import com.aquaai.intelligence.application.usecase.GetFieldTelemetrySignalsUseCase;
import com.aquaai.intelligence.application.usecase.ListAgronomicAssessmentsUseCase;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fields/{fieldId}/intelligence")
@RequiredArgsConstructor
public class IntelligenceController {

    private final EvaluateFieldIntelligenceUseCase evaluateFieldIntelligenceUseCase;
    private final GetFieldAgronomicAssessmentUseCase getFieldAgronomicAssessmentUseCase;
    private final GetFieldTelemetrySignalsUseCase getFieldTelemetrySignalsUseCase;
    private final ListAgronomicAssessmentsUseCase listAgronomicAssessmentsUseCase;

    @PostMapping("/evaluate")
    public ResponseEntity<ApiResponse<IntelligenceEvaluationResponse>> evaluate(@PathVariable UUID fieldId) {
        IntelligenceEvaluationResponse response = evaluateFieldIntelligenceUseCase.execute(fieldId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/assessment")
    public ResponseEntity<ApiResponse<AgronomicAssessmentResponse>> getAssessment(@PathVariable UUID fieldId) {
        AgronomicAssessmentResponse response = getFieldAgronomicAssessmentUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/signals")
    public ResponseEntity<ApiResponse<List<TelemetrySignalResponse>>> getSignals(@PathVariable UUID fieldId) {
        List<TelemetrySignalResponse> response = getFieldTelemetrySignalsUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PageResponse<AgronomicAssessmentResponse>>> getHistory(
            @PathVariable UUID fieldId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AgronomicAssessmentResponse> response = listAgronomicAssessmentsUseCase.execute(
                fieldId, from, to, page, size
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
