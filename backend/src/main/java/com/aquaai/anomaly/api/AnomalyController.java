package com.aquaai.anomaly.api;

import com.aquaai.anomaly.api.dto.ActionAnomalyRequest;
import com.aquaai.anomaly.api.dto.AnomalyResponse;
import com.aquaai.anomaly.api.dto.AnomalySummaryResponse;
import com.aquaai.anomaly.api.dto.FieldAnomalySummaryResponse;
import com.aquaai.anomaly.application.usecase.AcknowledgeAnomalyUseCase;
import com.aquaai.anomaly.application.usecase.DismissAnomalyUseCase;
import com.aquaai.anomaly.application.usecase.GetAnomalySummaryUseCase;
import com.aquaai.anomaly.application.usecase.GetAnomalyUseCase;
import com.aquaai.anomaly.application.usecase.GetFieldAnomalySummaryUseCase;
import com.aquaai.anomaly.application.usecase.InvestigateAnomalyUseCase;
import com.aquaai.anomaly.application.usecase.ListAnomaliesUseCase;
import com.aquaai.anomaly.application.usecase.ResolveAnomalyUseCase;
import com.aquaai.anomaly.domain.model.Anomaly;
import com.aquaai.anomaly.domain.model.AnomalySeverity;
import com.aquaai.anomaly.domain.model.AnomalyStatus;
import com.aquaai.anomaly.domain.model.AnomalyType;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class AnomalyController {

    private final GetAnomalyUseCase getAnomalyUseCase;
    private final ListAnomaliesUseCase listAnomaliesUseCase;
    private final AcknowledgeAnomalyUseCase acknowledgeAnomalyUseCase;
    private final InvestigateAnomalyUseCase investigateAnomalyUseCase;
    private final ResolveAnomalyUseCase resolveAnomalyUseCase;
    private final DismissAnomalyUseCase dismissAnomalyUseCase;
    private final GetAnomalySummaryUseCase getAnomalySummaryUseCase;
    private final GetFieldAnomalySummaryUseCase getFieldAnomalySummaryUseCase;

    @GetMapping("/anomalies")
    public ResponseEntity<ApiResponse<PageResponse<AnomalyResponse>>> listAnomalies(
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(required = false) UUID sensorId,
            @RequestParam(required = false) AnomalyStatus status,
            @RequestParam(required = false) AnomalySeverity severity,
            @RequestParam(required = false) AnomalyType anomalyType,
            @RequestParam(required = false) SensorType sensorType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListAnomaliesUseCase.Result result = listAnomaliesUseCase.execute(new ListAnomaliesUseCase.Query(
                fieldId, sensorId, status, severity, anomalyType, sensorType, from, to, page, size
        ));

        List<AnomalyResponse> responses = result.content().stream()
                .map(AnomalyResponse::fromDomain)
                .toList();

        int totalPages = result.totalElements() > 0 ? (int) Math.ceil((double) result.totalElements() / result.size()) : 0;

        PageResponse<AnomalyResponse> pageResponse = PageResponse.<AnomalyResponse>builder()
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

    @GetMapping("/anomalies/{id}")
    public ResponseEntity<ApiResponse<AnomalyResponse>> getAnomaly(@PathVariable UUID id) {
        Anomaly anomaly = getAnomalyUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyResponse.fromDomain(anomaly)));
    }

    @GetMapping("/fields/{fieldId}/anomalies")
    public ResponseEntity<ApiResponse<PageResponse<AnomalyResponse>>> listFieldAnomalies(
            @PathVariable UUID fieldId,
            @RequestParam(required = false) AnomalyStatus status,
            @RequestParam(required = false) AnomalySeverity severity,
            @RequestParam(required = false) AnomalyType anomalyType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListAnomaliesUseCase.Result result = listAnomaliesUseCase.execute(new ListAnomaliesUseCase.Query(
                fieldId, null, status, severity, anomalyType, null, null, null, page, size
        ));

        List<AnomalyResponse> responses = result.content().stream()
                .map(AnomalyResponse::fromDomain)
                .toList();

        int totalPages = result.totalElements() > 0 ? (int) Math.ceil((double) result.totalElements() / result.size()) : 0;

        PageResponse<AnomalyResponse> pageResponse = PageResponse.<AnomalyResponse>builder()
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

    @GetMapping("/sensors/{sensorId}/anomalies")
    public ResponseEntity<ApiResponse<PageResponse<AnomalyResponse>>> listSensorAnomalies(
            @PathVariable UUID sensorId,
            @RequestParam(required = false) AnomalyStatus status,
            @RequestParam(required = false) AnomalySeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ListAnomaliesUseCase.Result result = listAnomaliesUseCase.execute(new ListAnomaliesUseCase.Query(
                null, sensorId, status, severity, null, null, null, null, page, size
        ));

        List<AnomalyResponse> responses = result.content().stream()
                .map(AnomalyResponse::fromDomain)
                .toList();

        int totalPages = result.totalElements() > 0 ? (int) Math.ceil((double) result.totalElements() / result.size()) : 0;

        PageResponse<AnomalyResponse> pageResponse = PageResponse.<AnomalyResponse>builder()
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

    @PatchMapping("/anomalies/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AnomalyResponse>> acknowledge(@PathVariable UUID id) {
        Anomaly anomaly = acknowledgeAnomalyUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyResponse.fromDomain(anomaly)));
    }

    @PatchMapping("/anomalies/{id}/investigate")
    public ResponseEntity<ApiResponse<AnomalyResponse>> investigate(@PathVariable UUID id) {
        Anomaly anomaly = investigateAnomalyUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyResponse.fromDomain(anomaly)));
    }

    @PatchMapping("/anomalies/{id}/resolve")
    public ResponseEntity<ApiResponse<AnomalyResponse>> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ActionAnomalyRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        Anomaly anomaly = resolveAnomalyUseCase.execute(id, notes);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyResponse.fromDomain(anomaly)));
    }

    @PatchMapping("/anomalies/{id}/dismiss")
    public ResponseEntity<ApiResponse<AnomalyResponse>> dismiss(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ActionAnomalyRequest request
    ) {
        String notes = request != null ? request.notes() : null;
        Anomaly anomaly = dismissAnomalyUseCase.execute(id, notes);
        return ResponseEntity.ok(ApiResponse.ok(AnomalyResponse.fromDomain(anomaly)));
    }

    @GetMapping("/anomalies/summary")
    public ResponseEntity<ApiResponse<AnomalySummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(
                AnomalySummaryResponse.fromDomain(getAnomalySummaryUseCase.execute())
        ));
    }

    @GetMapping("/fields/{fieldId}/anomalies/summary")
    public ResponseEntity<ApiResponse<FieldAnomalySummaryResponse>> getFieldSummary(@PathVariable UUID fieldId) {
        return ResponseEntity.ok(ApiResponse.ok(
                FieldAnomalySummaryResponse.fromResult(getFieldAnomalySummaryUseCase.execute(fieldId))
        ));
    }
}
