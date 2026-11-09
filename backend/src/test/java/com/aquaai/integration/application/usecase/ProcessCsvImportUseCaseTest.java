package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.ImportBatchSummaryDto;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.application.service.IngestionPipelineService;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.DataSourceStatus;
import com.aquaai.integration.domain.model.DataSourceType;
import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.domain.repository.ImportBatchRepository;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessCsvImportUseCaseTest {

    @Mock
    private DataSourceRepository dataSourceRepository;
    @Mock
    private ImportBatchRepository importBatchRepository;
    @Mock
    private IngestionPipelineService ingestionPipelineService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private ProcessCsvImportUseCase csvImportUseCase;
    private DataSource csvSource;

    @BeforeEach
    void setUp() {
        csvImportUseCase = new ProcessCsvImportUseCase(
                dataSourceRepository,
                importBatchRepository,
                ingestionPipelineService,
                currentUserProvider
        );

        csvSource = DataSource.create(
                "CSV_TELEMETRY_IMPORT", "CSV Source", DataSourceType.CSV_IMPORT,
                "CsvAdapter", null, null, null, DataSourceStatus.ACTIVE
        );

        lenient().when(dataSourceRepository.findBySourceCode("CSV_TELEMETRY_IMPORT")).thenReturn(Optional.of(csvSource));
        lenient().when(importBatchRepository.save(any(ImportBatch.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Should successfully execute dry run preview without committing batch")
    void shouldExecuteDryRun() {
        UUID fieldId = UUID.randomUUID();
        String csvContent = "field_id,type,value,unit\n" +
                fieldId + ",SOIL_MOISTURE,55.4,%\n" +
                fieldId + ",WATER_FLOW,12.5,m³/h\n";

        var command = new ProcessCsvImportUseCase.CsvImportCommand(
                "telemetry.csv",
                csvContent.getBytes(StandardCharsets.UTF_8).length,
                new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)),
                true // dryRun
        );

        ImportBatchSummaryDto summary = csvImportUseCase.execute(command);

        assertThat(summary.totalRows()).isEqualTo(2);
        assertThat(summary.successfulRows()).isEqualTo(2);
        assertThat(summary.rejectedRows()).isEqualTo(0);
        assertThat(summary.fileName()).contains("DRY-RUN");

        // Verify no batch or pipeline save occurred
        verify(importBatchRepository, never()).save(any());
        verify(ingestionPipelineService, never()).processBatch(any());
    }

    @Test
    @DisplayName("Should execute real commit and track successful and rejected rows")
    void shouldExecuteRealCommit() {
        UUID fieldId = UUID.randomUUID();
        String csvContent = "field_id,type,value,unit\n" +
                fieldId + ",SOIL_MOISTURE,55.4,%\n" +
                fieldId + ",INVALID_TYPE,12.5,m³/h\n"; // 1 valid, 1 parse error

        when(ingestionPipelineService.processBatch(any())).thenReturn(IngestionResultDto.of(1, 1, 0, 0, List.of()));

        var command = new ProcessCsvImportUseCase.CsvImportCommand(
                "telemetry.csv",
                csvContent.getBytes(StandardCharsets.UTF_8).length,
                new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)),
                false // commit
        );

        ImportBatchSummaryDto summary = csvImportUseCase.execute(command);

        assertThat(summary.totalRows()).isEqualTo(2);
        assertThat(summary.successfulRows()).isEqualTo(1);
        assertThat(summary.rejectedRows()).isEqualTo(1);
        assertThat(summary.sampleErrors()).isNotEmpty();

        verify(importBatchRepository, atLeastOnce()).save(any());
        verify(importBatchRepository, times(1)).saveRowResults(any());
    }

    @Test
    @DisplayName("Should reject files exceeding size limit")
    void shouldRejectOversizedFile() {
        var command = new ProcessCsvImportUseCase.CsvImportCommand(
                "huge.csv",
                20 * 1024 * 1024, // 20 MB > 10 MB
                new ByteArrayInputStream(new byte[10]),
                true
        );

        assertThatThrownBy(() -> csvImportUseCase.execute(command))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("File size exceeds maximum allowed limit");
    }
}
