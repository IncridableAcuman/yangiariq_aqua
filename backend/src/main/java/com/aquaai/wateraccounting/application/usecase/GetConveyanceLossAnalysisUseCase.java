package com.aquaai.wateraccounting.application.usecase;

import com.aquaai.wateraccounting.api.dto.ConveyanceLossResponse;
import com.aquaai.wateraccounting.application.service.WaterAccountingDtoMapper;
import com.aquaai.wateraccounting.domain.model.EvidenceClassification;
import com.aquaai.wateraccounting.domain.port.AccountingNetworkPort;
import com.aquaai.wateraccounting.domain.service.ConveyanceLossEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetConveyanceLossAnalysisUseCase {

    private final ConveyanceLossEngine lossEngine;
    private final AccountingNetworkPort networkPort;
    private final WaterAccountingDtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<ConveyanceLossResponse> execute() {
        var edges = networkPort.getAllCanalEdges();
        List<ConveyanceLossResponse> results = new ArrayList<>();
        Instant now = Instant.now();
        Instant periodStart = now.minusSeconds(86400 * 30);

        for (var edge : edges) {
            BigDecimal maxCap = edge.maxCapacityM3S();
            BigDecimal flow = edge.currentFlowM3S().compareTo(BigDecimal.ZERO) > 0 ? edge.currentFlowM3S() : maxCap.multiply(new BigDecimal("0.65"));

            // Monthly volume approximation based on conveyance flow (flow m³/s * 3600 * 8h * 20 days)
            BigDecimal upstreamM3 = flow.multiply(new BigDecimal("576000")).setScale(2, RoundingMode.HALF_UP);

            // Loss factor based on canal length (meters) and type (earthen secondary canals have higher seepage)
            double lengthKm = edge.lengthMeters().doubleValue() / 1000.0;
            double lossRatio = Math.min(0.28, Math.max(0.03, lengthKm * 0.012));
            if ("SECONDARY_CANAL".equalsIgnoreCase(edge.edgeType())) {
                lossRatio += 0.04;
            } else if ("DISTRIBUTION_CHANNEL".equalsIgnoreCase(edge.edgeType())) {
                lossRatio += 0.07;
            }

            BigDecimal downstreamM3 = upstreamM3.multiply(BigDecimal.valueOf(1.0 - lossRatio)).setScale(2, RoundingMode.HALF_UP);

            var assessment = lossEngine.assessSegmentLoss(
                    edge.id(),
                    edge.name(),
                    edge.edgeType(),
                    edge.startNodeName(),
                    edge.endNodeName(),
                    periodStart,
                    now,
                    upstreamM3,
                    downstreamM3,
                    EvidenceClassification.CALCULATED,
                    "HIGH"
            );

            results.add(dtoMapper.toLossResponse(assessment));
        }

        return results;
    }
}
