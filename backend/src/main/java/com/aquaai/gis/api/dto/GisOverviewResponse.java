package com.aquaai.gis.api.dto;

import com.aquaai.water.api.dto.DateRangeDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Unified district GIS intelligence overview payload containing parcels, water infrastructure, volumetric statuses, and supply linkages")
public record GisOverviewResponse(
        @Schema(description = "List of accessible field parcels with geometry and agronomic attributes")
        List<GisFieldDto> fields,

        @Schema(description = "List of regional water sources and delivery infrastructure")
        List<GisWaterSourceDto> waterSources,

        @Schema(description = "Volumetric water statuses, quota consumption percentages, and alert categories per parcel")
        List<GisFieldWaterStatusDto> waterStatuses,

        @Schema(description = "Infrastructure-to-parcel allocation links for visual network routing")
        List<GisAllocationLinkDto> allocations,

        @Schema(description = "Query window period (null bounds if unbounded)")
        DateRangeDto period
) {
}
