# Regional Water Distribution Network Architecture

## 1. Architectural Scope & Context

**Milestone 08.14** elevates **YANGIARIQ AQUA AI** from individual parcel-level water management into district-level water distribution network intelligence, resilience analysis, and coordinated multi-parcel canal dispatch optimization.

Prior to Milestone 08.14, the platform operated at the parcel level (field boundaries, soil moisture telemetry, AI irrigation planning, hydraulic safety interlocks, and farm risk intelligence). In arid and semi-arid irrigation networks such as the **Amu Darya / Polvon-Shovot canal system** in the Yangiariq district of Khorezm, water delivery is physically constrained by primary canals, secondary conveyance branches, water user associations (WUA / SIU), control sluice gates, and pumping stations.

### High-Level Architectural Flow
```
REGIONAL WATER DEMAND (Multi-Parcel Plans)
          │
          ▼
NETWORK CAPACITY & TOPOLOGY VALIDATION
          │
          ▼
MULTI-PARCEL CONTENTION & CONFLICT DETECTION
          │
          ▼
DETERMINISTIC CANAL DISPATCH OPTIMIZATION (Fairness Policies)
          │
          ▼
MANDATORY HUMAN OPERATOR APPROVAL (Zero Autonomous Actuation)
          │
          ▼
OPERATIONAL EXECUTION & DISPATCH LOGGING
          │
          ▼
ACTUAL FLOW RECORDING & VARIANCE AUDITING (Planned vs Actual)
```

---

## 2. Strict Operational Principle: Decision-Support Only

> [!IMPORTANT]
> **Zero Autonomous Actuation**: YANGIARIQ AQUA AI is strictly a decision-support, simulation, and operational optimization platform. The system calculates, simulates, optimizes, and recommends canal dispatch schedules, but **MUST NOT autonomously operate physical pumps, valves, gates, or actuators**. All real-world dispatch operations require authorized human approval and manual gate confirmation.

---

## 3. Package & Domain Boundaries

The `com.aquaai.network` package follows Hexagonal / Clean Architecture and DDD principles:

```
com.aquaai.network
├── domain
│   ├── model
│   │   ├── WaterUserAssociation.java
│   │   ├── WaterNetworkNode.java (Aggregates WATER_SOURCE, PUMP_STATION, GATE, INLET)
│   │   ├── WaterNetworkEdge.java (PRIMARY_CANAL, SECONDARY_CANAL, DITCH, PIPELINE)
│   │   ├── NetworkCapacity.java
│   │   ├── RegionalWaterDemand.java
│   │   ├── DispatchSchedule.java (Aggregate Root: Proposed -> Approved -> Scheduled)
│   │   ├── DispatchAssignment.java
│   │   ├── DispatchConflict.java
│   │   ├── ActualDispatchDelivery.java
│   │   ├── ResilienceScenario.java
│   │   └── HydraulicSurgeRisk.java
│   ├── port
│   │   ├── NetworkFieldPort.java
│   │   ├── NetworkWaterPort.java
│   │   ├── NetworkIrrigationPort.java
│   │   ├── NetworkSensorPort.java
│   │   ├── NetworkAnomalyPort.java
│   │   └── NetworkNotificationPort.java
│   ├── repository
│   │   ├── WaterNetworkNodeRepository.java
│   │   ├── WaterNetworkEdgeRepository.java
│   │   ├── WaterUserAssociationRepository.java
│   │   ├── DispatchScheduleRepository.java
│   │   ├── ActualDispatchDeliveryRepository.java
│   │   └── ResilienceScenarioRepository.java
│   └── service
│       ├── NetworkTopologyValidator.java
│       ├── PathResolutionEngine.java
│       ├── NetworkCapacityEngine.java
│       ├── RegionalDemandAggregator.java
│       ├── CanalDispatchOptimizer.java
│       └── HydraulicResilienceEvaluator.java
├── application
│   ├── usecase
│   │   ├── GetWaterNetworkOverviewUseCase.java
│   │   ├── GetNetworkTopologyUseCase.java
│   │   ├── GetSegmentCapacityUseCase.java
│   │   ├── GetAffectedFieldsForSegmentUseCase.java
│   │   ├── GetRegionalDemandOverviewUseCase.java
│   │   ├── OptimizeCanalDispatchUseCase.java
│   │   ├── ListDispatchSchedulesUseCase.java
│   │   ├── GetDispatchScheduleDetailUseCase.java
│   │   ├── ApproveDispatchScheduleUseCase.java
│   │   ├── RejectDispatchScheduleUseCase.java
│   │   ├── ExecuteDispatchScheduleUseCase.java
│   │   ├── RecordActualDeliveryUseCase.java
│   │   ├── ListActualDeliveriesUseCase.java
│   │   ├── SimulateResilienceScenarioUseCase.java
│   │   ├── ListResilienceScenariosUseCase.java
│   │   └── GetResilienceScenarioDetailUseCase.java
│   └── service
│       └── WaterNetworkDtoMapper.java
├── infrastructure
│   ├── persistence
│   │   ├── WaterUserAssociationJpaEntity.java
│   │   ├── WaterNetworkNodeJpaEntity.java
│   │   ├── WaterNetworkEdgeJpaEntity.java
│   │   ├── DispatchScheduleJpaEntity.java
│   │   ├── ActualDispatchDeliveryJpaEntity.java
│   │   ├── ResilienceScenarioJpaEntity.java
│   │   └── NetworkDataInitializer.java (Yangiariq baseline seed)
│   ├── adapter
│   │   ├── NetworkFieldAdapter.java
│   │   ├── NetworkWaterAdapter.java
│   │   ├── NetworkIrrigationAdapter.java
│   │   ├── NetworkAnomalyAdapter.java
│   │   ├── NetworkSensorAdapter.java
│   │   └── InAppNetworkNotificationAdapter.java
│   └── config
│       └── NetworkInfrastructureConfig.java
└── api
    ├── WaterNetworkController.java
    ├── CanalDispatchController.java
    ├── ResilienceScenarioController.java
    └── dto/
```
