package com.aquaai.wateraccounting.infrastructure.persistence.mapper;

import com.aquaai.wateraccounting.domain.model.AccountingDimension;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.model.LedgerEntryType;
import com.aquaai.wateraccounting.domain.model.WaterLedgerEntry;
import com.aquaai.wateraccounting.infrastructure.persistence.entity.WaterLedgerEntryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class WaterLedgerEntryJpaMapper {

    public WaterLedgerEntry toDomain(WaterLedgerEntryJpaEntity entity) {
        if (entity == null) return null;

        return WaterLedgerEntry.builder()
                .id(entity.getId())
                .entryType(LedgerEntryType.valueOf(entity.getEntryType()))
                .accountingBoundary(AccountingDimension.valueOf(entity.getAccountingBoundary()))
                .volumeM3(entity.getVolumeM3())
                .flowRateM3S(entity.getFlowRateM3S())
                .sourceNodeOrCanal(entity.getSourceNodeOrCanal())
                .destinationNodeOrField(entity.getDestinationNodeOrField())
                .fieldId(entity.getFieldId())
                .wuaId(entity.getWuaId())
                .edgeId(entity.getEdgeId())
                .dispatchScheduleId(entity.getDispatchScheduleId())
                .allocationId(entity.getAllocationId())
                .eventTimestamp(entity.getEventTimestamp())
                .evidenceClassification(EvidenceClassification.valueOf(entity.getEvidenceClassification()))
                .dataSource(entity.getDataSource())
                .notes(entity.getNotes())
                .recordedBy(entity.getRecordedBy())
                .isCorrection(entity.isCorrection())
                .replacesEntryId(entity.getReplacesEntryId())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public WaterLedgerEntryJpaEntity toEntity(WaterLedgerEntry domain) {
        if (domain == null) return null;

        return WaterLedgerEntryJpaEntity.builder()
                .id(domain.getId())
                .entryType(domain.getEntryType().name())
                .accountingBoundary(domain.getAccountingBoundary().name())
                .volumeM3(domain.getVolumeM3())
                .flowRateM3S(domain.getFlowRateM3S())
                .sourceNodeOrCanal(domain.getSourceNodeOrCanal())
                .destinationNodeOrField(domain.getDestinationNodeOrField())
                .fieldId(domain.getFieldId())
                .wuaId(domain.getWuaId())
                .edgeId(domain.getEdgeId())
                .dispatchScheduleId(domain.getDispatchScheduleId())
                .allocationId(domain.getAllocationId())
                .eventTimestamp(domain.getEventTimestamp())
                .evidenceClassification(domain.getEvidenceClassification().name())
                .dataSource(domain.getDataSource())
                .notes(domain.getNotes())
                .recordedBy(domain.getRecordedBy())
                .isCorrection(domain.isCorrection())
                .replacesEntryId(domain.getReplacesEntryId())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
