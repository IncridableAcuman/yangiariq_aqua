package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.WaterLedgerEntryResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;
import com.aquaai.wateraccounting.domain.repository.WaterLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListWaterLedgerEntriesUseCase {

    private final WaterLedgerRepository ledgerRepository;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<WaterLedgerEntryResponse> execute(UUID fieldId, UUID wuaId, UUID edgeId, Instant start, Instant end) {
        List<WaterLedgerEntry> entries;

        if (fieldId != null) {
            entries = ledgerRepository.findByFieldId(fieldId);
        } else if (wuaId != null) {
            entries = ledgerRepository.findByWuaId(wuaId);
        } else if (edgeId != null) {
            entries = ledgerRepository.findByEdgeId(edgeId);
        } else if (start != null && end != null) {
            entries = ledgerRepository.findByPeriod(start, end);
        } else {
            entries = ledgerRepository.findAll();
        }

        return entries.stream()
                .map(dtoMapper::toLedgerResponse)
                .collect(Collectors.toList());
    }
}
