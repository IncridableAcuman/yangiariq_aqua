package com.aquaai.network.api.dto;

import java.util.List;

public record NetworkTopologyResponse(
        List<WaterNetworkNodeResponse> nodes,
        List<WaterNetworkEdgeResponse> edges,
        List<String> validationErrors,
        List<String> validationWarnings,
        boolean isValid
) {}
