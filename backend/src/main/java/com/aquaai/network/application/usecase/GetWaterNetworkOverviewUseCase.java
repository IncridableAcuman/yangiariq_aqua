package com.aquaai.network.application.usecase;

import com.aquaai.network.api.dto.WaterNetworkOverviewResponse;
import com.aquaai.network.domain.model.DispatchSchedule;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.repository.DispatchScheduleRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetWaterNetworkOverviewUseCase {

    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final DispatchScheduleRepository dispatchScheduleRepository;

    @Transactional(readOnly = true)
    public WaterNetworkOverviewResponse execute() {
        int totalNodes = (int) nodeRepository.count();
        List<WaterNetworkEdge> allEdges = edgeRepository.findAll();
        int totalEdges = allEdges.size();

        int operationalEdges = 0;
        int constrainedEdges = 0;
        int blockedEdges = 0;

        BigDecimal totalCapacity = BigDecimal.ZERO;
        BigDecimal totalFlow = BigDecimal.ZERO;
        List<String> surgeWarnings = new ArrayList<>();

        for (WaterNetworkEdge edge : allEdges) {
            if (edge.getOperationalStatus() == OperationalStatus.OPERATIONAL) {
                operationalEdges++;
            } else if (edge.getOperationalStatus() == OperationalStatus.CONSTRAINED) {
                constrainedEdges++;
            } else if (edge.getOperationalStatus() == OperationalStatus.BLOCKED) {
                blockedEdges++;
            }

            if (edge.getMaxCapacityM3S() != null) {
                totalCapacity = totalCapacity.add(edge.getMaxCapacityM3S());
            }
            if (edge.getCurrentFlowM3S() != null) {
                totalFlow = totalFlow.add(edge.getCurrentFlowM3S());
                if (edge.getMaxCapacityM3S() != null && edge.getMaxCapacityM3S().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal util = edge.getCurrentFlowM3S().divide(edge.getMaxCapacityM3S(), 4, RoundingMode.HALF_UP);
                    if (util.compareTo(new BigDecimal("0.90")) >= 0) {
                        surgeWarnings.add(String.format("Canal %s is operating at %s%% capacity (near overload threshold)",
                                edge.getName(), util.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP)));
                    }
                }
            }
        }

        BigDecimal utilizationPercent = BigDecimal.ZERO;
        if (totalCapacity.compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercent = totalFlow.multiply(new BigDecimal("100.00"))
                    .divide(totalCapacity, 2, RoundingMode.HALF_UP);
        }

        Optional<DispatchSchedule> latestActiveOpt = dispatchScheduleRepository.findLatestActiveSchedule();
        int activeScheduleCount = latestActiveOpt.isPresent() ? 1 : 0;
        String latestTitle = latestActiveOpt.map(DispatchSchedule::getTitle).orElse("No Active Dispatch Schedule");
        String latestStatus = latestActiveOpt.map(s -> s.getStatus().name()).orElse("NONE");

        return new WaterNetworkOverviewResponse(
                totalNodes,
                totalEdges,
                operationalEdges,
                constrainedEdges,
                blockedEdges,
                totalCapacity.setScale(2, RoundingMode.HALF_UP),
                totalFlow.setScale(2, RoundingMode.HALF_UP),
                utilizationPercent,
                activeScheduleCount,
                latestTitle,
                latestStatus,
                surgeWarnings
        );
    }
}
