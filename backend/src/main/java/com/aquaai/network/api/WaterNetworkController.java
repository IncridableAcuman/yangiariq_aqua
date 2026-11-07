package com.aquaai.network.api;

import com.aquaai.network.api.dto.AffectedFieldsResponse;
import com.aquaai.network.api.dto.NetworkTopologyResponse;
import com.aquaai.network.api.dto.RegionalDemandResponse;
import com.aquaai.network.api.dto.SegmentCapacityResponse;
import com.aquaai.network.api.dto.WaterNetworkOverviewResponse;
import com.aquaai.network.application.usecase.GetAffectedFieldsForSegmentUseCase;
import com.aquaai.network.application.usecase.GetNetworkTopologyUseCase;
import com.aquaai.network.application.usecase.GetRegionalDemandOverviewUseCase;
import com.aquaai.network.application.usecase.GetSegmentCapacityUseCase;
import com.aquaai.network.application.usecase.GetWaterNetworkOverviewUseCase;
import com.aquaai.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for Milestone 08.14: Regional Water Distribution Network Topology & Capacity.
 */
@RestController
@RequestMapping("/api/v1/water-network")
@RequiredArgsConstructor
public class WaterNetworkController {

    private final GetWaterNetworkOverviewUseCase getWaterNetworkOverviewUseCase;
    private final GetNetworkTopologyUseCase getNetworkTopologyUseCase;
    private final GetSegmentCapacityUseCase getSegmentCapacityUseCase;
    private final GetAffectedFieldsForSegmentUseCase getAffectedFieldsForSegmentUseCase;
    private final GetRegionalDemandOverviewUseCase getRegionalDemandOverviewUseCase;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<WaterNetworkOverviewResponse>> getOverview() {
        WaterNetworkOverviewResponse response = getWaterNetworkOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/topology")
    public ResponseEntity<ApiResponse<NetworkTopologyResponse>> getTopology() {
        NetworkTopologyResponse response = getNetworkTopologyUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/segments/{id}/capacity")
    public ResponseEntity<ApiResponse<SegmentCapacityResponse>> getSegmentCapacity(
            @PathVariable UUID id
    ) {
        SegmentCapacityResponse response = getSegmentCapacityUseCase.execute(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/affected-fields/{segmentId}")
    public ResponseEntity<ApiResponse<AffectedFieldsResponse>> getAffectedFields(
            @PathVariable UUID segmentId
    ) {
        AffectedFieldsResponse response = getAffectedFieldsForSegmentUseCase.execute(segmentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/demand/overview")
    public ResponseEntity<ApiResponse<RegionalDemandResponse>> getDemandOverview() {
        RegionalDemandResponse response = getRegionalDemandOverviewUseCase.execute();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
