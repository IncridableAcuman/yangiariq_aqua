package com.aquaai.field.api;

import com.aquaai.field.api.dto.CreateFieldRequest;
import com.aquaai.field.api.dto.FieldResponse;
import com.aquaai.field.api.dto.UpdateFieldRequest;
import com.aquaai.field.application.usecase.CreateFieldUseCase;
import com.aquaai.field.application.usecase.DeactivateFieldUseCase;
import com.aquaai.field.application.usecase.GetFieldUseCase;
import com.aquaai.field.application.usecase.ListMyFieldsUseCase;
import com.aquaai.field.application.usecase.ReactivateFieldUseCase;
import com.aquaai.field.application.usecase.UpdateFieldUseCase;
import com.aquaai.field.domain.model.CropType;
import com.aquaai.field.domain.model.FieldStatus;
import com.aquaai.field.domain.model.IrrigationMethod;
import com.aquaai.field.domain.model.SoilType;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.PageResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * REST controller exposing agricultural Field parcel operations.
 * Enforces authentication, role checks, and ownership-aware authorization.
 */
@RestController
@RequestMapping("/api/v1/fields")
@RequiredArgsConstructor
@Tag(name = "Field Management", description = "Endpoints for agricultural parcel registration, crop profiling, boundary mapping, and lifecycle management")
@SecurityRequirement(name = "bearerAuth")
public class FieldController {

    private final CreateFieldUseCase createFieldUseCase;
    private final GetFieldUseCase getFieldUseCase;
    private final ListMyFieldsUseCase listMyFieldsUseCase;
    private final UpdateFieldUseCase updateFieldUseCase;
    private final DeactivateFieldUseCase deactivateFieldUseCase;
    private final ReactivateFieldUseCase reactivateFieldUseCase;

    @PostMapping
    @PreAuthorize("hasRole('FARMER')")
    @Operation(
            summary = "Register a new agricultural field parcel",
            description = "Creates a field parcel owned by the authenticated farmer. Rejects duplicate active field names."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Field parcel created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or domain invariant violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires FARMER role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - active field with the same name already exists for this owner")
    })
    public ResponseEntity<ApiResponse<FieldResponse>> createField(
            @Valid @RequestBody CreateFieldRequest request
    ) {
        FieldResponse response = createFieldUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{fieldId}")
    @Operation(
            summary = "Get field parcel details by ID",
            description = "Retrieves field parcel information. Enforces ownership: Farmers access only their own fields; Operators and Admins have district access."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field parcel details retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user does not own this field parcel"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field parcel not found")
    })
    public ResponseEntity<ApiResponse<FieldResponse>> getFieldById(
            @PathVariable UUID fieldId
    ) {
        FieldResponse response = getFieldUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(
            summary = "List fields with filtering and pagination",
            description = "For FARMER, lists parcels owned by the authenticated user. For OPERATOR and ADMIN, lists parcels across the district with optional owner filtering."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fields retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT")
    })
    public ResponseEntity<ApiResponse<PageResponse<FieldResponse>>> listFields(
            @Parameter(description = "Filter by field lifecycle status")
            @RequestParam(required = false) FieldStatus status,
            @Parameter(description = "Filter by crop type")
            @RequestParam(required = false) CropType cropType,
            @Parameter(description = "Filter by soil type")
            @RequestParam(required = false) SoilType soilType,
            @Parameter(description = "Filter by irrigation method")
            @RequestParam(required = false) IrrigationMethod irrigationMethod,
            @Parameter(description = "Filter by owner ID (Admin and Operator only)")
            @RequestParam(required = false) UUID ownerId,
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 100)")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort parameter formatted as 'property,asc|desc' (e.g. createdAt,desc)")
            @RequestParam(required = false) String sort,
            @Parameter(description = "Sort property (createdAt, updatedAt, name, areaHectares, status)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc, desc)")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String resolvedSortBy = sortBy;
        String resolvedDirection = direction;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            resolvedSortBy = parts[0].trim();
            if (parts.length > 1) {
                resolvedDirection = parts[1].trim();
            }
        }

        PageResponse<FieldResponse> result = listMyFieldsUseCase.execute(
                status,
                cropType,
                soilType,
                irrigationMethod,
                ownerId,
                page,
                size,
                resolvedSortBy,
                resolvedDirection
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PatchMapping("/{fieldId}")
    @Operation(
            summary = "Update field parcel properties",
            description = "Updates editable field attributes using PATCH semantics. Missing attributes are preserved."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field parcel updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failure or domain invariant violation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user does not have permission to modify this field"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field parcel not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - active field with the updated name already exists")
    })
    public ResponseEntity<ApiResponse<FieldResponse>> updateField(
            @PathVariable UUID fieldId,
            @Valid @RequestBody UpdateFieldRequest request
    ) {
        FieldResponse response = updateFieldUseCase.execute(fieldId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{fieldId}")
    @Operation(
            summary = "Deactivate field parcel",
            description = "Transitions field status to INACTIVE. Preserves historical records (no physical deletion)."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field parcel deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user does not have permission to deactivate this field"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field parcel not found")
    })
    public ResponseEntity<ApiResponse<Void>> deactivateField(
            @PathVariable UUID fieldId
    ) {
        deactivateFieldUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PatchMapping("/{fieldId}/activate")
    @Operation(
            summary = "Reactivate an inactive field parcel",
            description = "Transitions field status from INACTIVE to ACTIVE after verifying name uniqueness."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Field parcel reactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - user does not have permission to reactivate this field"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Field parcel not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - active field with the same name already exists")
    })
    public ResponseEntity<ApiResponse<FieldResponse>> reactivateField(
            @PathVariable UUID fieldId
    ) {
        FieldResponse response = reactivateFieldUseCase.execute(fieldId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
