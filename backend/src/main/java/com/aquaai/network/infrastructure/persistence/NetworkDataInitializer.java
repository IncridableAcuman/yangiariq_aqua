package com.aquaai.network.infrastructure.persistence;

import com.aquaai.field.domain.model.CoordinatePoint;
import com.aquaai.field.domain.model.Field;
import com.aquaai.field.domain.repository.FieldRepository;
import com.aquaai.network.domain.model.OperationalStatus;
import com.aquaai.network.domain.model.WaterNetworkEdge;
import com.aquaai.network.domain.model.WaterNetworkEdgeType;
import com.aquaai.network.domain.model.WaterNetworkNode;
import com.aquaai.network.domain.model.WaterNetworkNodeType;
import com.aquaai.network.domain.model.WaterUserAssociation;
import com.aquaai.network.domain.repository.WaterNetworkEdgeRepository;
import com.aquaai.network.domain.repository.WaterNetworkNodeRepository;
import com.aquaai.network.domain.repository.WaterUserAssociationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Initializes baseline Yangiariq regional water distribution network topology on first startup.
 */
@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class NetworkDataInitializer implements ApplicationRunner {

    private final WaterUserAssociationRepository wuaRepository;
    private final WaterNetworkNodeRepository nodeRepository;
    private final WaterNetworkEdgeRepository edgeRepository;
    private final FieldRepository fieldRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (nodeRepository.count() > 0) {
            log.info("Water network nodes already initialized (count={}). Skipping seeding.", nodeRepository.count());
            return;
        }

        log.info("Initializing baseline Yangiariq regional water distribution network topology...");

        // 1. Water User Associations
        WaterUserAssociation wua1 = WaterUserAssociation.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555501"))
                .name("Yangiariq Suv Ziyosi SIU")
                .code("WUA-YNG-01")
                .district("Yangiariq")
                .contactPerson("Olimov Bahodir")
                .phoneNumber("+998901234567")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        wuaRepository.save(wua1);

        WaterUserAssociation wua2 = WaterUserAssociation.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555502"))
                .name("Bog'ot Obihayot SIU")
                .code("WUA-BGT-02")
                .district("Yangiariq")
                .contactPerson("Karimov Rustam")
                .phoneNumber("+998909876543")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        wuaRepository.save(wua2);

        // 2. Nodes
        UUID sourceNodeId = UUID.fromString("22222222-3333-4444-5555-666666666601");
        WaterNetworkNode sourceNode = WaterNetworkNode.builder()
                .id(sourceNodeId)
                .name("Tuyamuyun Bosh Suv Olgich")
                .code("NODE-SRC-01")
                .type(WaterNetworkNodeType.WATER_SOURCE)
                .latitude(new BigDecimal("41.385000"))
                .longitude(new BigDecimal("60.620000"))
                .elevationMeters(new BigDecimal("95.00"))
                .wuaId(wua1.getId())
                .isActive(true)
                .properties(Map.of("sourceType", "RIVER_INTAKE", "capacity", "12.5 m3/s"))
                .build();
        nodeRepository.save(sourceNode);

        UUID pumpNodeId = UUID.fromString("22222222-3333-4444-5555-666666666602");
        WaterNetworkNode pumpNode = WaterNetworkNode.builder()
                .id(pumpNodeId)
                .name("Yangiariq-1 Nasos Stansiyasi")
                .code("NODE-PMP-01")
                .type(WaterNetworkNodeType.PUMP_STATION)
                .latitude(new BigDecimal("41.372000"))
                .longitude(new BigDecimal("60.605000"))
                .elevationMeters(new BigDecimal("98.50"))
                .wuaId(wua1.getId())
                .isActive(true)
                .properties(Map.of("powerKw", 450, "pumpUnits", 4))
                .build();
        nodeRepository.save(pumpNode);

        UUID junctionNodeId = UUID.fromString("22222222-3333-4444-5555-666666666603");
        WaterNetworkNode junctionNode = WaterNetworkNode.builder()
                .id(junctionNodeId)
                .name("Polvon Bosh Taqsimlagich")
                .code("NODE-JNC-01")
                .type(WaterNetworkNodeType.CANAL_JUNCTION)
                .latitude(new BigDecimal("41.365000"))
                .longitude(new BigDecimal("60.590000"))
                .elevationMeters(new BigDecimal("97.00"))
                .wuaId(wua1.getId())
                .isActive(true)
                .properties(Collections.emptyMap())
                .build();
        nodeRepository.save(junctionNode);

        UUID gateNodeId = UUID.fromString("22222222-3333-4444-5555-666666666604");
        WaterNetworkNode gateNode = WaterNetworkNode.builder()
                .id(gateNodeId)
                .name("Darvoza-1 Nazorat Shlyuzi")
                .code("NODE-GAT-01")
                .type(WaterNetworkNodeType.CONTROL_GATE)
                .latitude(new BigDecimal("41.355000"))
                .longitude(new BigDecimal("60.580000"))
                .elevationMeters(new BigDecimal("96.00"))
                .wuaId(wua2.getId())
                .isActive(true)
                .properties(Map.of("gateType", "RADIAL_SLUICE", "maxOpeningMeters", 2.5))
                .build();
        nodeRepository.save(gateNode);

        UUID distNodeId = UUID.fromString("22222222-3333-4444-5555-666666666605");
        WaterNetworkNode distNode = WaterNetworkNode.builder()
                .id(distNodeId)
                .name("Chigirtka Suv Taqsimlagich")
                .code("NODE-DST-01")
                .type(WaterNetworkNodeType.DISTRIBUTION_POINT)
                .latitude(new BigDecimal("41.348000"))
                .longitude(new BigDecimal("60.570000"))
                .elevationMeters(new BigDecimal("95.50"))
                .wuaId(wua2.getId())
                .isActive(true)
                .properties(Collections.emptyMap())
                .build();
        nodeRepository.save(distNode);

        UUID termNodeId = UUID.fromString("22222222-3333-4444-5555-666666666606");
        WaterNetworkNode termNode = WaterNetworkNode.builder()
                .id(termNodeId)
                .name("Yangiariq Kollektor Chiqishi")
                .code("NODE-TRM-01")
                .type(WaterNetworkNodeType.TERMINAL_NODE)
                .latitude(new BigDecimal("41.335000"))
                .longitude(new BigDecimal("60.550000"))
                .elevationMeters(new BigDecimal("92.00"))
                .wuaId(wua2.getId())
                .isActive(true)
                .properties(Collections.emptyMap())
                .build();
        nodeRepository.save(termNode);

        // 3. Edges
        List<CoordinatePoint> geom1 = List.of(
                CoordinatePoint.of(60.620000, 41.385000),
                CoordinatePoint.of(60.612500, 41.378000),
                CoordinatePoint.of(60.605000, 41.372000)
        );
        WaterNetworkEdge edge1 = WaterNetworkEdge.builder()
                .id(UUID.fromString("33333333-4444-5555-6666-777777777701"))
                .name("Amudaryo-Tuyamuyun Magistral Kanali")
                .code("EDGE-PRI-01")
                .type(WaterNetworkEdgeType.PRIMARY_CANAL)
                .startNodeId(sourceNodeId)
                .endNodeId(pumpNodeId)
                .wuaId(wua1.getId())
                .lengthMeters(new BigDecimal("4200.00"))
                .maxCapacityM3S(new BigDecimal("8.5000"))
                .minFlowM3S(new BigDecimal("0.5000"))
                .currentFlowM3S(new BigDecimal("3.2000"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .geometry(geom1)
                .build();
        edgeRepository.save(edge1);

        List<CoordinatePoint> geom2 = List.of(
                CoordinatePoint.of(60.605000, 41.372000),
                CoordinatePoint.of(60.598000, 41.368000),
                CoordinatePoint.of(60.590000, 41.365000)
        );
        WaterNetworkEdge edge2 = WaterNetworkEdge.builder()
                .id(UUID.fromString("33333333-4444-5555-6666-777777777702"))
                .name("Polvon Yuqori Kanali")
                .code("EDGE-PRI-02")
                .type(WaterNetworkEdgeType.PRIMARY_CANAL)
                .startNodeId(pumpNodeId)
                .endNodeId(junctionNodeId)
                .wuaId(wua1.getId())
                .lengthMeters(new BigDecimal("3100.00"))
                .maxCapacityM3S(new BigDecimal("5.2000"))
                .minFlowM3S(new BigDecimal("0.2000"))
                .currentFlowM3S(new BigDecimal("2.1000"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .geometry(geom2)
                .build();
        edgeRepository.save(edge2);

        List<CoordinatePoint> geom3 = List.of(
                CoordinatePoint.of(60.590000, 41.365000),
                CoordinatePoint.of(60.585000, 41.360000),
                CoordinatePoint.of(60.580000, 41.355000)
        );
        WaterNetworkEdge edge3 = WaterNetworkEdge.builder()
                .id(UUID.fromString("33333333-4444-5555-6666-777777777703"))
                .name("Shovot-Yangiariq Tarmoq Kanali")
                .code("EDGE-SEC-01")
                .type(WaterNetworkEdgeType.SECONDARY_CANAL)
                .startNodeId(junctionNodeId)
                .endNodeId(gateNodeId)
                .wuaId(wua2.getId())
                .lengthMeters(new BigDecimal("2800.00"))
                .maxCapacityM3S(new BigDecimal("2.8000"))
                .minFlowM3S(new BigDecimal("0.1000"))
                .currentFlowM3S(new BigDecimal("1.4000"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .geometry(geom3)
                .build();
        edgeRepository.save(edge3);

        List<CoordinatePoint> geom4 = List.of(
                CoordinatePoint.of(60.580000, 41.355000),
                CoordinatePoint.of(60.575000, 41.351000),
                CoordinatePoint.of(60.570000, 41.348000)
        );
        WaterNetworkEdge edge4 = WaterNetworkEdge.builder()
                .id(UUID.fromString("33333333-4444-5555-6666-777777777704"))
                .name("Chigirtka Tarqatish Arog'i")
                .code("EDGE-DST-01")
                .type(WaterNetworkEdgeType.DISTRIBUTION_CHANNEL)
                .startNodeId(gateNodeId)
                .endNodeId(distNodeId)
                .wuaId(wua2.getId())
                .lengthMeters(new BigDecimal("1900.00"))
                .maxCapacityM3S(new BigDecimal("1.2000"))
                .minFlowM3S(new BigDecimal("0.0500"))
                .currentFlowM3S(new BigDecimal("0.6500"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .geometry(geom4)
                .build();
        edgeRepository.save(edge4);

        List<CoordinatePoint> geom5 = List.of(
                CoordinatePoint.of(60.570000, 41.348000),
                CoordinatePoint.of(60.560000, 41.340000),
                CoordinatePoint.of(60.550000, 41.335000)
        );
        WaterNetworkEdge edge5 = WaterNetworkEdge.builder()
                .id(UUID.fromString("33333333-4444-5555-6666-777777777705"))
                .name("Kollektorga Chiqish Oqimi")
                .code("EDGE-TRM-01")
                .type(WaterNetworkEdgeType.DISTRIBUTION_CHANNEL)
                .startNodeId(distNodeId)
                .endNodeId(termNodeId)
                .wuaId(wua2.getId())
                .lengthMeters(new BigDecimal("2400.00"))
                .maxCapacityM3S(new BigDecimal("1.5000"))
                .minFlowM3S(new BigDecimal("0.0000"))
                .currentFlowM3S(new BigDecimal("0.2000"))
                .operationalStatus(OperationalStatus.OPERATIONAL)
                .geometry(geom5)
                .build();
        edgeRepository.save(edge5);

        // Connect actual fields to the distribution network
        List<Field> allFields = fieldRepository.findAll();
        int inletIndex = 1;
        for (Field f : allFields) {
            BigDecimal lat = f.getLocation() != null ? f.getLocation().latitude() : new BigDecimal("41.345000");
            BigDecimal lon = f.getLocation() != null ? f.getLocation().longitude() : new BigDecimal("60.565000");

            UUID inletId = UUID.randomUUID();
            WaterNetworkNode inletNode = WaterNetworkNode.builder()
                    .id(inletId)
                    .name("Dala Kirish Shlyuzi - " + f.getName())
                    .code("INLET-FLD-" + String.format("%02d", inletIndex))
                    .type(WaterNetworkNodeType.FIELD_INLET)
                    .latitude(lat)
                    .longitude(lon)
                    .elevationMeters(new BigDecimal("94.80"))
                    .fieldId(f.getId())
                    .wuaId(wua2.getId())
                    .isActive(true)
                    .properties(Map.of("fieldCrop", f.getCropType().name(), "areaHa", f.getAreaHectares()))
                    .build();
            nodeRepository.save(inletNode);

            // Connect distNode -> inletNode
            List<CoordinatePoint> pipelineGeom = List.of(
                    CoordinatePoint.of(distNode.getLongitude().doubleValue(), distNode.getLatitude().doubleValue()),
                    CoordinatePoint.of(lon.doubleValue(), lat.doubleValue())
            );
            WaterNetworkEdge pipelineEdge = WaterNetworkEdge.builder()
                    .id(UUID.randomUUID())
                    .name("Dala Tarqatish Quvuri - " + f.getName())
                    .code("EDGE-PIP-" + String.format("%02d", inletIndex))
                    .type(WaterNetworkEdgeType.PIPELINE)
                    .startNodeId(distNodeId)
                    .endNodeId(inletId)
                    .wuaId(wua2.getId())
                    .lengthMeters(new BigDecimal("650.00"))
                    .maxCapacityM3S(new BigDecimal("0.4500"))
                    .minFlowM3S(new BigDecimal("0.0100"))
                    .currentFlowM3S(new BigDecimal("0.1200"))
                    .operationalStatus(OperationalStatus.OPERATIONAL)
                    .geometry(pipelineGeom)
                    .build();
            edgeRepository.save(pipelineEdge);

            inletIndex++;
        }

        log.info("Initialized Yangiariq network: nodes={}, edges={}, wuas=2", nodeRepository.count(), edgeRepository.count());
    }
}
