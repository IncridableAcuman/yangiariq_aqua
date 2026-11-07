package com.aquaai.wateraccounting.domain.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AccountingNetworkPort {

    List<CanalEdgeSummary> getAllCanalEdges();

    List<DeliverySummary> getAllActualDeliveries();

    List<DeliverySummary> getDeliveriesForField(UUID fieldId);

    List<WuaSummary> getAllWuas();

    Optional<WuaSummary> getWuaById(UUID wuaId);

    Map<UUID, UUID> getFieldToWuaMapping();

    record CanalEdgeSummary(
            UUID id,
            String name,
            String code,
            String edgeType,
            UUID startNodeId,
            String startNodeName,
            UUID endNodeId,
            String endNodeName,
            BigDecimal maxCapacityM3S,
            BigDecimal currentFlowM3S,
            BigDecimal lengthMeters
    ) {}

    record DeliverySummary(
            UUID id,
            UUID dispatchScheduleId,
            UUID fieldId,
            String fieldName,
            UUID edgeId,
            String canalName,
            BigDecimal plannedVolumeM3,
            BigDecimal actualVolumeM3,
            BigDecimal measuredFlowM3S,
            Instant actualStartAt,
            Instant actualEndAt
    ) {}

    record WuaSummary(
            UUID id,
            String name,
            String code,
            String district
    ) {}
}
