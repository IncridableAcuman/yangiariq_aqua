package com.aquaai.integration.api;

import com.aquaai.integration.application.dto.ImportBatchSummaryDto;
import com.aquaai.integration.application.usecase.GetImportBatchesUseCase;
import com.aquaai.integration.application.usecase.ProcessCsvImportUseCase;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-imports")
@RequiredArgsConstructor
@Tag(name = "Batch Data Imports", description = "Endpoints for batch CSV telemetry file ingestion, preview validation, and import audit reports")
@SecurityRequirement(name = "bearerAuth")
public class DataImportController {

    private final ProcessCsvImportUseCase processCsvImportUseCase;
    private final GetImportBatchesUseCase getImportBatchesUseCase;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Upload and process CSV telemetry file", description = "Parses and ingests telemetry batch file. Supports dryRun=true for preview validation without saving.")
    public ResponseEntity<ApiResponse<ImportBatchSummaryDto>> uploadCsv(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun
    ) throws IOException {
        ProcessCsvImportUseCase.CsvImportCommand command = new ProcessCsvImportUseCase.CsvImportCommand(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "telemetry.csv",
                file.getSize(),
                file.getInputStream(),
                dryRun
        );

        ImportBatchSummaryDto summary = processCsvImportUseCase.execute(command);
        HttpStatus status = dryRun ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(ApiResponse.ok(summary));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "List batch import executions", description = "Returns historical CSV batch upload results")
    public ResponseEntity<ApiResponse<List<ImportBatchSummaryDto>>> listBatches(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit
    ) {
        List<ImportBatchSummaryDto> batches = getImportBatchesUseCase.listBatches(offset, limit);
        return ResponseEntity.ok(ApiResponse.ok(batches));
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Get batch import details", description = "Retrieves row-level validation errors and stats for a batch import")
    public ResponseEntity<ApiResponse<ImportBatchSummaryDto>> getBatch(@PathVariable UUID batchId) {
        ImportBatchSummaryDto batch = getImportBatchesUseCase.getBatch(batchId);
        return ResponseEntity.ok(ApiResponse.ok(batch));
    }
}
