package com.aquaai.integration.api;

import com.aquaai.integration.api.dto.CreateDataSourceRequest;
import com.aquaai.integration.api.dto.UpdateDataSourceRequest;
import com.aquaai.integration.application.dto.DataSourceDto;
import com.aquaai.integration.application.usecase.GetDataSourceUseCase;
import com.aquaai.integration.application.usecase.ListDataSourcesUseCase;
import com.aquaai.integration.application.usecase.RegisterDataSourceUseCase;
import com.aquaai.integration.application.usecase.ToggleDataSourceStatusUseCase;
import com.aquaai.integration.application.usecase.TriggerDataSourceSyncUseCase;
import com.aquaai.integration.application.usecase.UpdateDataSourceUseCase;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/data-sources")
@RequiredArgsConstructor
@Tag(name = "Data Source Registry", description = "Endpoints for managing real-world external integrations and telemetry sources")
@SecurityRequirement(name = "bearerAuth")
public class DataSourceController {

    private final ListDataSourcesUseCase listDataSourcesUseCase;
    private final GetDataSourceUseCase getDataSourceUseCase;
    private final RegisterDataSourceUseCase registerDataSourceUseCase;
    private final UpdateDataSourceUseCase updateDataSourceUseCase;
    private final ToggleDataSourceStatusUseCase toggleDataSourceStatusUseCase;
    private final TriggerDataSourceSyncUseCase triggerDataSourceSyncUseCase;

    @GetMapping
    @Operation(summary = "List all data sources", description = "Returns all registered external and internal telemetry/meteorological data sources")
    public ResponseEntity<ApiResponse<List<DataSourceDto>>> listDataSources() {
        List<DataSourceDto> sources = listDataSourcesUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(sources));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get data source details", description = "Retrieves configuration and sync status for a specific data source")
    public ResponseEntity<ApiResponse<DataSourceDto>> getDataSource(@PathVariable UUID id) {
        DataSourceDto source = getDataSourceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(source));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Register a new data source", description = "Creates a new external data source integration (Restricted to OPERATOR and ADMIN)")
    public ResponseEntity<ApiResponse<DataSourceDto>> registerDataSource(
            @Valid @RequestBody CreateDataSourceRequest request
    ) {
        DataSourceDto created = registerDataSourceUseCase.execute(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Update data source metadata", description = "Updates configuration reference, endpoint URL, or description")
    public ResponseEntity<ApiResponse<DataSourceDto>> updateDataSource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDataSourceRequest request
    ) {
        DataSourceDto updated = updateDataSourceUseCase.execute(id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Toggle data source status", description = "Toggles data source between ACTIVE and INACTIVE")
    public ResponseEntity<ApiResponse<DataSourceDto>> toggleDataSource(@PathVariable UUID id) {
        DataSourceDto updated = toggleDataSourceStatusUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @PostMapping("/{id}/sync")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(summary = "Trigger manual synchronization", description = "Triggers immediate out-of-band sync for an external data source")
    public ResponseEntity<ApiResponse<DataSourceDto>> triggerSync(@PathVariable UUID id) {
        DataSourceDto updated = triggerDataSourceSyncUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }
}
