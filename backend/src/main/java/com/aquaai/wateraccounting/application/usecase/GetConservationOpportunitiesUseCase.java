package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.ConservationOpportunityResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceItem;
import com.aquaai.wateraccounting.domain.model.AllocationComplianceStatus;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossAssessment;
import com.aquaai.wateraccounting.domain.model.ConveyanceLossClassification;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.repository.WaterReconciliationRepository;
import com.aquaai.wateraccounting.domain.service.ConservationIntelligenceEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetConservationOpportunitiesUseCase {

    private final ConservationIntelligenceEngine conservationEngine;
    private final GetConveyanceLossAnalysisUseCase lossUseCase;
    private final GetAllocationComplianceUseCase complianceUseCase;
    private final WaterReconciliationRepository reconciliationRepository;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<ConservationOpportunityResponse> execute() {
        var lossResponses = lossUseCase.execute();
        List<ConveyanceLossAssessment> lossAssessments = lossResponses.stream()
                .map(r -> ConveyanceLossAssessment.builder()
                        .edgeId(r.edgeId())
                        .canalName(r.canalName())
                        .edgeType(r.edgeType())
                        .startNodeName(r.startNodeName())
                        .endNodeName(r.endNodeName())
                        .periodStart(r.periodStart())
                        .periodEnd(r.periodEnd())
                        .upstreamVolumeM3(r.upstreamVolumeM3())
                        .downstreamVolumeM3(r.downstreamVolumeM3())
                        .lossVolumeM3(r.lossVolumeM3())
                        .lossPercentage(r.lossPercentage())
                        .classification(ConveyanceLossClassification.valueOf(r.classification()))
                        .evidenceClassification(EvidenceClassification.valueOf(r.evidenceClassification()))
                        .confidence(r.confidence())
                        .explanation(r.explanation())
                        .build())
                .collect(Collectors.toList());

        var compResponses = complianceUseCase.execute(null);
        List<AllocationComplianceItem> complianceItems = compResponses.stream()
                .map(c -> AllocationComplianceItem.builder()
                        .targetId(c.targetId())
                        .targetName(c.targetName())
                        .targetType(c.targetType())
                        .wuaName(c.wuaName())
                        .cropType(c.cropType())
                        .areaHectares(c.areaHectares())
                        .allocatedVolumeM3(c.allocatedVolumeM3())
                        .reservedVolumeM3(c.reservedVolumeM3())
                        .approvedDispatchVolumeM3(c.approvedDispatchVolumeM3())
                        .actualDeliveredVolumeM3(c.actualDeliveredVolumeM3())
                        .actualConsumptionM3(c.actualConsumptionM3())
                        .remainingAllocationM3(c.remainingAllocationM3())
                        .overDeliveryM3(c.overDeliveryM3())
                        .underDeliveryM3(c.underDeliveryM3())
                        .deliveryCompletionPercent(c.deliveryCompletionPercent())
                        .status(AllocationComplianceStatus.valueOf(c.status()))
                        .evidenceClassification(EvidenceClassification.valueOf(c.evidenceClassification()))
                        .notes(c.notes())
                        .build())
                .collect(Collectors.toList());

        var reconciliations = reconciliationRepository.findAll();

        var opportunities = conservationEngine.identifyOpportunities(lossAssessments, complianceItems, reconciliations);

        return opportunities.stream()
                .map(dtoMapper::toOpportunityResponse)
                .collect(Collectors.toList());
    }
}
