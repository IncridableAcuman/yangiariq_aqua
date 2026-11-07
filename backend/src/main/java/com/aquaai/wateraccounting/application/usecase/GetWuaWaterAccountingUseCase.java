package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.wateraccounting.api.dto.AllocationComplianceResponse;
import com.aquaai.wateraccounting.api.dto.ConservationOpportunityResponse;
import com.aquaai.wateraccounting.api.dto.ConveyanceLossResponse;
import com.aquaai.wateraccounting.api.dto.DistributionEquityResponse;
import com.aquaai.wateraccounting.api.dto.RegionalWaterBalanceResponse;
import com.aquaai.wateraccounting.api.dto.WuaWaterAccountingResponse;
import com.aquaai.wateraccounting.domain.port.AccountingNetworkPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetWuaWaterAccountingUseCase {

    private final AccountingNetworkPort networkPort;
    private final GetRegionalWaterBalanceUseCase balanceUseCase;
    private final GetDistributionEquityUseCase equityUseCase;
    private final GetAllocationComplianceUseCase complianceUseCase;
    private final GetConveyanceLossAnalysisUseCase lossUseCase;
    private final GetConservationOpportunitiesUseCase opportunitiesUseCase;

    @Transactional(readOnly = true)
    public WuaWaterAccountingResponse execute(UUID wuaId) {
        var wua = networkPort.getWuaById(wuaId)
                .orElseThrow(() -> new ResourceNotFoundException("Water User Association", wuaId));

        RegionalWaterBalanceResponse balance = balanceUseCase.execute("WUA", wuaId, "SEASONAL", null, null);

        DistributionEquityResponse equity = equityUseCase.execute().stream()
                .filter(e -> wuaId.equals(e.clusterId()))
                .findFirst()
                .orElse(null);

        List<AllocationComplianceResponse> fieldCompliances = complianceUseCase.execute(null).stream()
                .filter(c -> wua.name().equalsIgnoreCase(c.wuaName()))
                .collect(Collectors.toList());

        List<ConveyanceLossResponse> canalLosses = lossUseCase.execute();

        List<ConservationOpportunityResponse> opportunities = opportunitiesUseCase.execute().stream()
                .filter(o -> wuaId.equals(o.affectedComponentId()) || "WUA".equalsIgnoreCase(o.affectedComponentType()))
                .collect(Collectors.toList());

        return new WuaWaterAccountingResponse(
                wua.id(),
                wua.name(),
                wua.code(),
                wua.district(),
                balance,
                equity,
                fieldCompliances,
                canalLosses,
                opportunities
        );
    }
}
