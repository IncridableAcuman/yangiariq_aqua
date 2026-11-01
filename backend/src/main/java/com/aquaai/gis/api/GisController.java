package com.aquaai.gis.api;

import com.aquaai.gis.api.dto.GisOverviewResponse;
import com.aquaai.gis.application.usecase.GetGisOverviewUseCase;
import com.aquaai.shared.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * REST controller for unified spatial GIS intelligence.
 * Provides high-performance read models combining field polygons, water sources,
 * volumetric water balance statuses, and operational allocation linkages.
 */
@RestController
@RequestMapping("/api/v1/gis")
@RequiredArgsConstructor
@Tag(name = "GIS Intelligence & Mapping", description = "High-performance operational geospatial endpoints combining parcel boundaries, water sources, and consumption accounting")
@SecurityRequirement(name = "bearerAuth")
public class GisController {

    private final GetGisOverviewUseCase getGisOverviewUseCase;

    @GetMapping("/overview")
    @Operation(
            summary = "Get unified GIS spatial and water intelligence overview",
            description = "Returns accessible parcel polygons, water infrastructure markers, real-time water balance posturing, and allocation links. Farmers receive strictly their own parcels; Operators/Admins monitor district-wide assets."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "GIS overview payload retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid date range parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated - missing or invalid JWT"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business rule violation - e.g. 'from' timestamp after 'to' timestamp")
    })
    public ResponseEntity<ApiResponse<GisOverviewResponse>> getOverview(
            @Parameter(description = "Optional filter window start timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,

            @Parameter(description = "Optional filter window end timestamp in ISO-8601 UTC")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        GisOverviewResponse response = getGisOverviewUseCase.execute(from, to);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
