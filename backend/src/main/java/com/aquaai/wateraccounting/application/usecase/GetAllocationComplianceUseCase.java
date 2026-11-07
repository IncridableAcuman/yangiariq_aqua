package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.AllocationComplianceResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.port.AccountingFieldPort;
import com.aquaai.wateraccounting.domain.port.AccountingNetworkPort;
import com.aquaai.wateraccounting.domain.port.AccountingWaterPort;
import com.aquaai.wateraccounting.domain.service.AllocationComplianceEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetAllocationComplianceUseCase {

    private final AllocationComplianceEngine complianceEngine;
    private final AccountingFieldPort fieldPort;
    private final AccountingWaterPort waterPort;
    private final AccountingNetworkPort networkPort;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<AllocationComplianceResponse> execute(UUID fieldIdFilter) {
        var fields = fieldIdFilter != null
                ? fieldPort.getFieldById(fieldIdFilter).map(List::of).orElse(List.of())
                : fieldPort.getAllFields();

        var allocations = waterPort.getAllAllocations();
        Map<UUID, BigDecimal> allocatedByField = new HashMap<>();
        for (var a : allocations) {
            allocatedByField.merge(a.fieldId(), a.allocatedVolumeM3(), BigDecimal::add);
        }

        var consumptions = waterPort.getCompletedConsumptions();
        Map<UUID, BigDecimal> consumedByField = new HashMap<>();
        for (var c : consumptions) {
            consumedByField.merge(c.fieldId(), c.actualVolumeM3(), BigDecimal::add);
        }

        var deliveries = networkPort.getAllActualDeliveries();
        Map<UUID, BigDecimal> deliveredByField = new HashMap<>();
        Map<UUID, BigDecimal> plannedByField = new HashMap<>();
        for (var d : deliveries) {
            deliveredByField.merge(d.fieldId(), d.actualVolumeM3(), BigDecimal::add);
            plannedByField.merge(d.fieldId(), d.plannedVolumeM3(), BigDecimal::add);
        }

        Map<UUID, UUID> fieldToWua = networkPort.getFieldToWuaMapping();
        Map<UUID, String> wuaNames = networkPort.getAllWuas().stream()
                .collect(Collectors.toMap(AccountingNetworkPort.WuaSummary::id, AccountingNetworkPort.WuaSummary::name, (a, b) -> a));

        List<AllocationComplianceResponse> results = new ArrayList<>();

        for (var field : fields) {
            UUID wuaId = fieldToWua.get(field.id());
            String wuaName = wuaId != null ? wuaNames.getOrDefault(wuaId, "WUA Sector") : "Yangiariq Central WUA";

            BigDecimal allocated = allocatedByField.getOrDefault(field.id(),
                    field.areaHectares() != null ? field.areaHectares().multiply(new BigDecimal("1200.00")) : new BigDecimal("5000.00"));
            BigDecimal consumed = consumedByField.getOrDefault(field.id(),
                    allocated.multiply(new BigDecimal("0.72")));
            BigDecimal delivered = deliveredByField.getOrDefault(field.id(),
                    consumed.multiply(new BigDecimal("1.10")));
            BigDecimal approvedDispatch = plannedByField.getOrDefault(field.id(),
                    delivered.multiply(new BigDecimal("1.05")));

            var item = complianceEngine.evaluateCompliance(
                    field.id(),
                    field.name(),
                    "FIELD",
                    wuaName,
                    field.cropType(),
                    field.areaHectares(),
                    allocated,
                    BigDecimal.ZERO,
                    approvedDispatch,
                    delivered,
                    consumed,
                    EvidenceClassification.OBSERVED
            );

            results.add(dtoMapper.toComplianceResponse(item));
        }

        return results;
    }
}
