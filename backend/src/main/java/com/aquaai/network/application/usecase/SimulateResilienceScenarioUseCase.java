package com.aquaai.network.application.usecase;

import com.aquaai.irrigation.domain.model.SmartIrrigationPlan;
import com.aquaai.network.api.dto.ResilienceScenarioRequest;
import com.aquaai.network.api.dto.ResilienceScenarioResponse;
import com.aquaai.network.application.service.WaterNetworkDtoMapper;
import com.aquaai.network.domain.model.ResilienceScenario;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.port.NetworkFieldPort;
import com.aquaai.network.domain.port.NetworkIrrigationPort;
import com.aquaai.network.domain.repository.ResilienceScenarioRepository;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.service.HydraulicResilienceEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulateResilienceScenarioUseCase {

    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final NetworkFieldPort fieldPort;
    private final NetworkIrrigationPort irrigationPort;
    private final ResilienceScenarioRepository scenarioRepository;
    private final HydraulicResilienceEvaluator resilienceEvaluator;
    private final WaterNetworkDtoMapper dtoMapper;

    @Transactional
    public ResilienceScenarioResponse execute(ResilienceScenarioRequest request, UUID currentUserId) {
        List<WaterNetworkNode> allNodes = nodeRepository.findAll();
        List<WaterNetworkEdge> allEdges = edgeRepository.findAll();
        List<NetworkFieldPort.FieldParcelInfo> fields = fieldPort.getAllFields();

        Map<UUID, BigDecimal> fieldDemandMap = new HashMap<>();
        for (NetworkFieldPort.FieldParcelInfo f : fields) {
            List<SmartIrrigationPlan> plans = irrigationPort.getUpcomingPlansForField(f.id());
            BigDecimal demand = BigDecimal.ZERO;
            for (SmartIrrigationPlan p : plans) {
                if (p.getRecommendedVolumeM3() != null) {
                    demand = demand.add(p.getRecommendedVolumeM3());
                }
            }
            if (demand.compareTo(BigDecimal.ZERO) == 0) {
                demand = f.areaHectares() != null ? f.areaHectares().multiply(new BigDecimal("150.00")) : new BigDecimal("300.00");
            }
            fieldDemandMap.put(f.id(), demand);
        }

        ResilienceScenario scenario = resilienceEvaluator.simulateScenario(
                request.name(),
                request.scenarioType(),
                request.disabledNodeIds(),
                request.disabledEdgeIds(),
                request.capacityReductionPercent(),
                request.demandSurgePercent(),
                allNodes,
                allEdges,
                fieldDemandMap,
                currentUserId
        );

        ResilienceScenario saved = scenarioRepository.save(scenario);
        log.info("Simulated and saved isolated resilience scenario: id={}, name='{}', type='{}', affectedFieldCount={}",
                saved.getId(), saved.getName(), saved.getScenarioType(), saved.getAffectedFieldCount());

        return dtoMapper.toScenarioResponse(saved);
    }
}
