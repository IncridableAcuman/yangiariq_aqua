package com.aquaai.integration.application.usecase;

import com.aquaai.integration.application.dto.ImportBatchSummaryDto;
import com.aquaai.integration.application.dto.ImportRowResultDto;
import com.aquaai.integration.application.dto.IngestBatchCommand;
import com.aquaai.integration.application.dto.IngestTelemetryItemCommand;
import com.aquaai.integration.application.dto.IngestionResultDto;
import com.aquaai.integration.application.service.IngestionPipelineService;
import com.aquaai.integration.domain.model.DataSource;
import com.aquaai.integration.domain.model.ImportBatch;
import com.aquaai.integration.domain.model.ImportRowResult;
import com.aquaai.integration.domain.repository.DataSourceRepository;
import com.aquaai.integration.domain.repository.ImportBatchRepository;
import com.aquaai.sensor.domain.model.SensorType;
import com.aquaai.shared.domain.BusinessRuleException;
import com.aquaai.shared.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessCsvImportUseCase {

    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    public static final int MAX_ROWS = 10000;

    private final DataSourceRepository dataSourceRepository;
    private final ImportBatchRepository importBatchRepository;
    private final IngestionPipelineService ingestionPipelineService;
    private final CurrentUserProvider currentUserProvider;

    public record CsvImportCommand(
            String fileName,
            long fileSizeBytes,
            InputStream inputStream,
            boolean dryRun
    ) {}

    @Transactional
    public ImportBatchSummaryDto execute(CsvImportCommand command) {
        if (command == null || command.inputStream() == null) {
            throw new BusinessRuleException("Import file input stream is required", "EMPTY_IMPORT_STREAM");
        }
        if (command.fileSizeBytes() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException(
                    "File size exceeds maximum allowed limit of 10MB (" + command.fileSizeBytes() + " bytes)",
                    "FILE_SIZE_EXCEEDED"
            );
        }

        UUID currentUserId = null;
        try {
            currentUserId = currentUserProvider.getRequiredUserId();
        } catch (Exception ignored) {}

        DataSource dataSource = dataSourceRepository.findBySourceCode("CSV_TELEMETRY_IMPORT")
                .orElseGet(() -> {
                    DataSource defaultSource = DataSource.create(
                            "CSV_TELEMETRY_IMPORT",
                            "CSV Telemetry Ingestion Gateway",
                            com.aquaai.integration.domain.model.DataSourceType.CSV_IMPORT,
                            "CsvTelemetryImporter",
                            "Controlled batch file ingestion for sensor telemetry",
                            null,
                            "INTERNAL:csv",
                            com.aquaai.integration.domain.model.DataSourceStatus.ACTIVE
                    );
                    return dataSourceRepository.save(defaultSource);
                });

        List<ParsedRow> parsedRows;
        try {
            parsedRows = parseCsv(command.inputStream());
        } catch (Exception e) {
            throw new BusinessRuleException("Failed to parse CSV file: " + e.getMessage(), "CSV_PARSING_FAILED");
        }

        if (parsedRows.isEmpty()) {
            throw new BusinessRuleException("CSV file contains no data rows", "EMPTY_CSV_DATA");
        }
        if (parsedRows.size() > MAX_ROWS) {
            throw new BusinessRuleException(
                    "CSV contains " + parsedRows.size() + " rows, exceeding safety limit of " + MAX_ROWS,
                    "ROW_COUNT_EXCEEDED"
            );
        }

        if (command.dryRun()) {
            return simulateDryRun(dataSource, parsedRows, command.fileName(), command.fileSizeBytes(), currentUserId);
        }

        // Real execution
        ImportBatch batch = ImportBatch.create(
                dataSource.getId(),
                command.fileName(),
                command.fileSizeBytes(),
                currentUserId
        );
        ImportBatch savedBatch = importBatchRepository.save(batch);

        List<ImportRowResult> rowResults = new ArrayList<>();
        int successCount = 0;
        int rejectCount = 0;
        List<ImportRowResultDto> sampleErrors = new ArrayList<>();

        for (ParsedRow row : parsedRows) {
            if (row.parseError != null) {
                rejectCount++;
                rowResults.add(ImportRowResult.failure(savedBatch.getId(), row.rowNumber, row.externalRecordId, row.parseError));
                if (sampleErrors.size() < 10) {
                    sampleErrors.add(new ImportRowResultDto(row.rowNumber, false, row.externalRecordId, row.parseError));
                }
                continue;
            }

            IngestTelemetryItemCommand item = new IngestTelemetryItemCommand(
                    row.externalRecordId,
                    row.fieldId,
                    row.sensorId,
                    row.deviceIdentifier,
                    row.sensorType,
                    row.value,
                    row.unit,
                    row.measuredAt,
                    "OBSERVED"
            );

            IngestionResultDto res = ingestionPipelineService.processBatch(
                    new IngestBatchCommand("CSV_TELEMETRY_IMPORT", List.of(item))
            );

            if (res.validCount() > 0) {
                successCount++;
                rowResults.add(ImportRowResult.success(savedBatch.getId(), row.rowNumber, row.externalRecordId));
            } else {
                rejectCount++;
                String err = res.errors().isEmpty() ? "Rejected by quality validation" : res.errors().get(0);
                rowResults.add(ImportRowResult.failure(savedBatch.getId(), row.rowNumber, row.externalRecordId, err));
                if (sampleErrors.size() < 10) {
                    sampleErrors.add(new ImportRowResultDto(row.rowNumber, false, row.externalRecordId, err));
                }
            }
        }

        importBatchRepository.saveRowResults(rowResults);
        savedBatch.complete(parsedRows.size(), successCount, rejectCount);
        importBatchRepository.save(savedBatch);

        return new ImportBatchSummaryDto(
                savedBatch.getId(),
                savedBatch.getFileName(),
                savedBatch.getFileSizeBytes(),
                savedBatch.getTotalRows(),
                savedBatch.getSuccessfulRows(),
                savedBatch.getRejectedRows(),
                savedBatch.getStatus(),
                savedBatch.getStartedAt(),
                savedBatch.getCompletedAt(),
                sampleErrors
        );
    }

    private ImportBatchSummaryDto simulateDryRun(
            DataSource dataSource,
            List<ParsedRow> rows,
            String fileName,
            long fileSizeBytes,
            UUID userId
    ) {
        int successCount = 0;
        int rejectCount = 0;
        List<ImportRowResultDto> sampleErrors = new ArrayList<>();

        for (ParsedRow row : rows) {
            if (row.parseError != null) {
                rejectCount++;
                if (sampleErrors.size() < 10) {
                    sampleErrors.add(new ImportRowResultDto(row.rowNumber, false, row.externalRecordId, row.parseError));
                }
            } else {
                successCount++;
            }
        }

        String status = (rejectCount == 0 && successCount > 0) ? "COMPLETED" : (successCount > 0 ? "PARTIAL" : "FAILED");
        return new ImportBatchSummaryDto(
                UUID.randomUUID(),
                fileName + " (DRY-RUN)",
                fileSizeBytes,
                rows.size(),
                successCount,
                rejectCount,
                status,
                Instant.now(),
                Instant.now(),
                sampleErrors
        );
    }

    private record ParsedRow(
            int rowNumber,
            String externalRecordId,
            UUID fieldId,
            UUID sensorId,
            String deviceIdentifier,
            SensorType sensorType,
            BigDecimal value,
            String unit,
            Instant measuredAt,
            String parseError
    ) {}

    private List<ParsedRow> parseCsv(InputStream is) throws Exception {
        List<ParsedRow> rows = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        String headerLine = reader.readLine();
        if (headerLine == null) {
            return rows;
        }

        String[] headers = splitCsvLine(headerLine);
        Map<String, Integer> colMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            colMap.put(headers[i].trim().toLowerCase().replaceAll("[^a-z0-9_]", ""), i);
        }

        int fieldIdIdx = findHeaderIndex(colMap, "fieldid", "field_id", "parcelid", "parcel_id");
        int typeIdx = findHeaderIndex(colMap, "type", "sensortype", "sensor_type");
        int valueIdx = findHeaderIndex(colMap, "value", "reading_value", "reading", "val");
        int unitIdx = findHeaderIndex(colMap, "unit", "measurement_unit", "units");
        int timeIdx = findHeaderIndex(colMap, "timestamp", "measuredat", "measured_at", "time", "date");
        int sensorIdIdx = findHeaderIndex(colMap, "sensorid", "sensor_id");
        int devIdIdx = findHeaderIndex(colMap, "deviceidentifier", "device_identifier", "device_id", "dev_id");
        int recordIdIdx = findHeaderIndex(colMap, "externalrecordid", "recordid", "record_id", "id");

        if (fieldIdIdx == -1 || typeIdx == -1 || valueIdx == -1) {
            throw new IllegalArgumentException("CSV missing required headers: must contain field_id, type, and value");
        }

        String line;
        int rowNumber = 1;
        while ((line = reader.readLine()) != null) {
            rowNumber++;
            if (line.trim().isEmpty()) continue;

            String[] tokens = splitCsvLine(line);
            try {
                UUID fieldId = UUID.fromString(getToken(tokens, fieldIdIdx));
                SensorType sensorType = SensorType.valueOf(getToken(tokens, typeIdx).toUpperCase());
                BigDecimal value = new BigDecimal(getToken(tokens, valueIdx));
                String unit = (unitIdx != -1) ? getToken(tokens, unitIdx) : null;
                Instant measuredAt = (timeIdx != -1 && !getToken(tokens, timeIdx).isEmpty())
                        ? parseInstantSafe(getToken(tokens, timeIdx))
                        : Instant.now();
                UUID sensorId = (sensorIdIdx != -1 && !getToken(tokens, sensorIdIdx).isEmpty())
                        ? UUID.fromString(getToken(tokens, sensorIdIdx))
                        : null;
                String deviceIdentifier = (devIdIdx != -1) ? getToken(tokens, devIdIdx) : null;
                String recordId = (recordIdIdx != -1) ? getToken(tokens, recordIdIdx) : "CSV-" + rowNumber;

                rows.add(new ParsedRow(rowNumber, recordId, fieldId, sensorId, deviceIdentifier, sensorType, value, unit, measuredAt, null));
            } catch (Exception e) {
                String recordId = (recordIdIdx != -1 && tokens.length > recordIdIdx) ? tokens[recordIdIdx] : "ROW-" + rowNumber;
                rows.add(new ParsedRow(rowNumber, recordId, null, null, null, null, null, null, null, e.getMessage()));
            }
        }

        return rows;
    }

    private static int findHeaderIndex(Map<String, Integer> map, String... candidates) {
        for (String c : candidates) {
            if (map.containsKey(c)) {
                return map.get(c);
            }
        }
        return -1;
    }

    private static String getToken(String[] tokens, int idx) {
        if (idx >= 0 && idx < tokens.length) {
            return tokens[idx].trim();
        }
        return "";
    }

    private static Instant parseInstantSafe(String raw) {
        try {
            return Instant.parse(raw);
        } catch (Exception e) {
            // Fallback for epoch millis or seconds
            if (raw.matches("^\\d+$")) {
                long num = Long.parseLong(raw);
                if (num > 100000000000L) {
                    return Instant.ofEpochMilli(num);
                }
                return Instant.ofEpochSecond(num);
            }
            throw e;
        }
    }

    private static String[] splitCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString().trim());
        return list.toArray(new String[0]);
    }
}
