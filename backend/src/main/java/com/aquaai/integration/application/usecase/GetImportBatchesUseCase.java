package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.ImportBatchSummaryDto;
import com.aquaai.integration.application.dto.ImportRowResultDto;
import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.repository.ImportBatchRepository;
import com.aquaai.shared.domain.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetImportBatchesUseCase {

    private final ImportBatchRepository importBatchRepository;

    @Transactional(readOnly = true)
    public List<ImportBatchSummaryDto> listBatches(int offset, int limit) {
        return importBatchRepository.findAll(offset, limit).stream()
                .map(this::toSummaryWithoutErrors)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportBatchSummaryDto getBatch(UUID batchId) {
        ImportBatch batch = importBatchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("ImportBatch", batchId));

        List<ImportRowResultDto> errors = importBatchRepository.findRowResultsByBatchId(batchId, 0, 50).stream()
                .filter(r -> !r.isSuccess())
                .map(r -> new ImportRowResultDto(r.getRowNumber(), false, r.getExternalRecordId(), r.getErrorMessage()))
                .toList();

        return new ImportBatchSummaryDto(
                batch.getId(),
                batch.getFileName(),
                batch.getFileSizeBytes(),
                batch.getTotalRows(),
                batch.getSuccessfulRows(),
                batch.getRejectedRows(),
                batch.getStatus(),
                batch.getStartedAt(),
                batch.getCompletedAt(),
                errors
        );
    }

    private ImportBatchSummaryDto toSummaryWithoutErrors(ImportBatch batch) {
        return new ImportBatchSummaryDto(
                batch.getId(),
                batch.getFileName(),
                batch.getFileSizeBytes(),
                batch.getTotalRows(),
                batch.getSuccessfulRows(),
                batch.getRejectedRows(),
                batch.getStatus(),
                batch.getStartedAt(),
                batch.getCompletedAt(),
                List.of()
        );
    }
}
