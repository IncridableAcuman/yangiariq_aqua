package com.aquaai.network.infrastructure.persistence;

import com.aquaai.network.domain.model.ResilienceScenario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResilienceScenarioJpaMapper {

    private final ObjectMapper objectMapper;

    public ResilienceScenarioJpaEntity toJpaEntity(ResilienceScenario domain) {
        if (domain == null) return null;

        String nodesJson = "[]";
        String edgesJson = "[]";
        String wuasJson = "[]";
        String resultsJson = "{}";

        try {
            if (domain.getDisabledNodeIds() != null) {
                nodesJson = objectMapper.writeValueAsString(domain.getDisabledNodeIds());
            }
            if (domain.getDisabledEdgeIds() != null) {
                edgesJson = objectMapper.writeValueAsString(domain.getDisabledEdgeIds());
            }
            if (domain.getAffectedWuaNames() != null) {
                wuasJson = objectMapper.writeValueAsString(domain.getAffectedWuaNames());
            }
            if (domain.getSimulationResults() != null) {
                resultsJson = objectMapper.writeValueAsString(domain.getSimulationResults());
            }
        } catch (Exception e) {
            log.error("Failed to serialize scenario JSON: id={}", domain.getId(), e);
        }

        return ResilienceScenarioJpaEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .scenarioType(domain.getScenarioType())
                .disabledNodeIdsJson(nodesJson)
                .disabledEdgeIdsJson(edgesJson)
                .capacityReductionPercent(domain.getCapacityReductionPercent())
                .demandSurgePercent(domain.getDemandSurgePercent())
                .affectedFieldCount(domain.getAffectedFieldCount())
                .lostCapacityM3S(domain.getLostCapacityM3S())
                .unmetDemandM3(domain.getUnmetDemandM3())
                .affectedWuaNamesJson(wuasJson)
                .alternativeFeasibleRoutesCount(domain.getAlternativeFeasibleRoutesCount())
                .summary(domain.getSummary())
                .simulationResultsJson(resultsJson)
                .createdBy(domain.getCreatedBy())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public ResilienceScenario toDomain(ResilienceScenarioJpaEntity entity) {
        if (entity == null) return null;

        List<UUID> disabledNodes = Collections.emptyList();
        List<UUID> disabledEdges = Collections.emptyList();
        List<String> affectedWuas = Collections.emptyList();
        Map<String, Object> results = Collections.emptyMap();

        try {
            if (entity.getDisabledNodeIdsJson() != null && !entity.getDisabledNodeIdsJson().isBlank()) {
                disabledNodes = objectMapper.readValue(entity.getDisabledNodeIdsJson(), new TypeReference<List<UUID>>() {});
            }
            if (entity.getDisabledEdgeIdsJson() != null && !entity.getDisabledEdgeIdsJson().isBlank()) {
                disabledEdges = objectMapper.readValue(entity.getDisabledEdgeIdsJson(), new TypeReference<List<UUID>>() {});
            }
            if (entity.getAffectedWuaNamesJson() != null && !entity.getAffectedWuaNamesJson().isBlank()) {
                affectedWuas = objectMapper.readValue(entity.getAffectedWuaNamesJson(), new TypeReference<List<String>>() {});
            }
            if (entity.getSimulationResultsJson() != null && !entity.getSimulationResultsJson().isBlank()) {
                results = objectMapper.readValue(entity.getSimulationResultsJson(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("Failed to deserialize scenario JSON: id={}", entity.getId(), e);
        }

        return ResilienceScenario.builder()
                .id(entity.getId())
                .name(entity.getName())
                .scenarioType(entity.getScenarioType())
                .disabledNodeIds(disabledNodes)
                .disabledEdgeIds(disabledEdges)
                .capacityReductionPercent(entity.getCapacityReductionPercent())
                .demandSurgePercent(entity.getDemandSurgePercent())
                .affectedFieldCount(entity.getAffectedFieldCount())
                .lostCapacityM3S(entity.getLostCapacityM3S())
                .unmetDemandM3(entity.getUnmetDemandM3())
                .affectedWuaNames(affectedWuas)
                .alternativeFeasibleRoutesCount(entity.getAlternativeFeasibleRoutesCount())
                .summary(entity.getSummary())
                .simulationResults(results)
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
