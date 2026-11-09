package com.aquaai.integration.api;

import com.aquaai.integration.api.dto.IngestTelemetryBatchRequest;
import com.aquaai.integration.api.dto.RecordManualMeasurementRequest;
import com.aquaai.integration.application.dto.IngestedProvenanceDto;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.application.service.IngestionPipelineService;
import com.aquaai.integration.application.usecase.GetIngestionHistoryUseCase;
import com.aquaai.integration.application.usecase.RecordManualMeasurementUseCase;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-ingestion")
@RequiredArgsConstructor
@Tag(name = "Data Ingestion Pipeline", description = "Endpoints for normalized telemetry batch ingestion, manual measurement entry, and provenance tracking")
@SecurityRequirement(name = "bearerAuth")
public class DataIngestionController {

    private final IngestionPipelineService ingestionPipelineService;
    private final RecordManualMeasurementUseCase recordManualMeasurementUseCase;
    private final GetIngestionHistoryUseCase getIngestionHistoryUseCase;

    @PostMapping("/telemetry")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Ingest external telemetry batch", description = "Receives telemetry records from external HTTP IoT gateways or integration jobs")
    public ResponseEntity<ApiResponse<IngestionResultDto>> ingestBatch(
            @Valid @RequestBody IngestTelemetryBatchRequest request
    ) {
        IngestionResultDto result = ingestionPipelineService.processBatch(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @PostMapping("/manual")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'FARMER')")
    @Operation(summary = "Submit manual measurement", description = "Records a manual field measurement with explicit verification classification")
    public ResponseEntity<ApiResponse<IngestionResultDto>> recordManual(
            @Valid @RequestBody RecordManualMeasurementRequest request
    ) {
        IngestionResultDto result = recordManualMeasurementUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @GetMapping("/history")
    @Operation(summary = "View ingestion provenance history", description = "Retrieves recent telemetry observations with full data quality classification and evidence attribution")
    public ResponseEntity<ApiResponse<List<IngestedProvenanceDto>>> getHistory(
            @RequestParam(required = false) UUID dataSourceId,
            @RequestParam(required = false) UUID fieldId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<IngestedProvenanceDto> history = getIngestionHistoryUseCase.execute(dataSourceId, fieldId, limit);
        return ResponseEntity.ok(ApiResponse.ok(history));
    }
}
