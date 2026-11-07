package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.shared.domain.ResourceNotFoundException;
import com.aquaai.wateraccounting.api.dto.ConservationOpportunityResponse;
import com.aquaai.wateraccounting.api.dto.FieldWaterAccountingResponse;
import com.aquaai.wateraccounting.api.dto.WaterLedgerEntryResponse;
import com.aquaai.wateraccounting.domain.port.AccountingFieldPort;
import com.aquaai.wateraccounting.domain.port.AccountingNetworkPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFieldWaterAccountingUseCase {

    private final AccountingFieldPort fieldPort;
    private final AccountingNetworkPort networkPort;
    private final GetAllocationComplianceUseCase complianceUseCase;
    private final ListWaterLedgerEntriesUseCase ledgerUseCase;
    private final GetConservationOpportunitiesUseCase opportunitiesUseCase;

    @Transactional(readOnly = true)
    public FieldWaterAccountingResponse execute(UUID fieldId) {
        var field = fieldPort.getFieldById(fieldId)
                .orElseThrow(() -> new ResourceNotFoundException("Field parcel", fieldId));

        Map<UUID, UUID> fieldToWua = networkPort.getFieldToWuaMapping();
        UUID wuaId = fieldToWua.get(field.id());
        String wuaName = "Yangiariq Central WUA";
        if (wuaId != null) {
            wuaName = networkPort.getWuaById(wuaId)
                    .map(AccountingNetworkPort.WuaSummary::name)
                    .orElse("WUA " + wuaId);
        }

        var compliances = complianceUseCase.execute(fieldId);
        var compliance = compliances.isEmpty() ? null : compliances.get(0);

        List<WaterLedgerEntryResponse> recentMovements = ledgerUseCase.execute(fieldId, null, null, null, null);

        List<ConservationOpportunityResponse> recommendations = opportunitiesUseCase.execute().stream()
                .filter(o -> fieldId.equals(o.affectedComponentId()) || "FIELD".equalsIgnoreCase(o.affectedComponentType()))
                .collect(Collectors.toList());

        return new FieldWaterAccountingResponse(
                field.id(),
                field.name(),
                field.ownerId(),
                wuaName,
                field.areaHectares(),
                field.cropType(),
                compliance,
                recentMovements,
                recommendations
        );
    }
}
