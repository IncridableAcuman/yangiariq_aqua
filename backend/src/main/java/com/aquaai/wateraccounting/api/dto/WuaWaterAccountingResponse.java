package com.aquaai.wateraccounting.api.dto;

import java.util.List;
import java.util.UUID;

public record WuaWaterAccountingResponse(
        UUID wuaId,
        String wuaName,
        String wuaCode,
        String district,
        RegionalWaterBalanceResponse waterBalance,
        DistributionEquityResponse equityAssessment,
        List<AllocationComplianceResponse> fieldCompliances,
        List<ConveyanceLossResponse> canalLosses,
        List<ConservationOpportunityResponse> conservationOpportunities
) {}
