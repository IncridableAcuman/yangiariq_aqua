package com.aquaai.water.api;

import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
import com.aquaai.water.api.dto.CreateWaterSourceRequest;
import com.aquaai.water.api.dto.UpdateWaterSourceRequest;
import com.aquaai.water.api.dto.WaterSourceResponse;
import com.aquaai.water.application.usecase.ActivateWaterSourceUseCase;
import com.aquaai.water.application.usecase.CreateWaterSourceUseCase;
import com.aquaai.water.application.usecase.DeactivateWaterSourceUseCase;
import com.aquaai.water.application.usecase.GetWaterSourceUseCase;
import com.aquaai.water.application.usecase.ListWaterSourcesUseCase;
import com.aquaai.water.application.usecase.UpdateWaterSourceUseCase;
import com.aquaai.water.domain.model.WaterSourceStatus;
import com.aquaai.water.domain.model.WaterSourceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for regional water source infrastructure entities (canals, ditches, reservoirs, wells).
 * Administrative mutations restricted to OPERATOR and ADMIN roles. Read operations open to all authenticated users.
 */
@RestController
@RequestMapping("/api/v1/water-sources")
@RequiredArgsConstructor
@Tag(name = "Water Source Infrastructure", description = "Endpoints for managing regional water delivery infrastructure")
@SecurityRequirement(name = "bearerAuth")
public class WaterSourceController {

    private final CreateWaterSourceUseCase createWaterSourceUseCase;
    private final GetWaterSourceUseCase getWaterSourceUseCase;
    private final ListWaterSourcesUseCase listWaterSourcesUseCase;
    private final UpdateWaterSourceUseCase updateWaterSourceUseCase;
    private final ActivateWaterSourceUseCase activateWaterSourceUseCase;
    private final DeactivateWaterSourceUseCase deactivateWaterSourceUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(
            summary = "Register a new regional water source",
            description = "Creates a water source infrastructure entity. Restricted to OPERATOR and ADMIN roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Water source registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires OPERATOR or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - active water source with same name already exists")
    })
    public ResponseEntity<ApiResponse<WaterSourceResponse>> createWaterSource(
            @Valid @RequestBody CreateWaterSourceRequest request
    ) {
        WaterSourceResponse response = createWaterSourceUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get water source details by ID",
            description = "Retrieves information for a specific water source. Open to all authenticated users."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water source details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water source not found")
    })
    public ResponseEntity<ApiResponse<WaterSourceResponse>> getWaterSourceById(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response = getWaterSourceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(
            summary = "List regional water sources with filtering and pagination",
            description = "Retrieves a paginated list of water sources with optional type, status, and name substring search."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water sources retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT")
    })
    public ResponseEntity<ApiResponse<PageResponse<WaterSourceResponse>>> listWaterSources(
            @Parameter(description = "Case-insensitive name search query")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by hydraulic type")
            @RequestParam(required = false) WaterSourceType type,

            @Parameter(description = "Filter by operational status")
            @RequestParam(required = false) WaterSourceStatus status,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort property", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction ('asc' or 'desc')", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        PageResponse<WaterSourceResponse> response = listWaterSourcesUseCase.execute(
                search,
                type,
                status,
                page,
                size,
                sortBy,
                direction
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(
            summary = "Update water source details",
            description = "Updates water source name, type, and description. Restricted to OPERATOR and ADMIN roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water source updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or invalid domain parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires OPERATOR or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water source not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - active water source with same name already exists")
    })
    public ResponseEntity<ApiResponse<WaterSourceResponse>> updateWaterSource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWaterSourceRequest request
    ) {
        WaterSourceResponse response = updateWaterSourceUseCase.execute(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(
            summary = "Activate a water source",
            description = "Sets water source status to ACTIVE. Restricted to OPERATOR and ADMIN roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water source activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires OPERATOR or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water source not found")
    })
    public ResponseEntity<ApiResponse<WaterSourceResponse>> activateWaterSource(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response = activateWaterSourceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    @Operation(
            summary = "Deactivate a water source",
            description = "Sets water source status to INACTIVE. Restricted to OPERATOR and ADMIN roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Water source deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires OPERATOR or ADMIN role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Water source not found")
    })
    public ResponseEntity<ApiResponse<WaterSourceResponse>> deactivateWaterSource(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response = deactivateWaterSourceUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
