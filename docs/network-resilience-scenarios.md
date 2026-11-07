# Regional Water Network Resilience & What-If Stress Scenarios

## 1. Resilience Modeling Philosophy

Agricultural water networks are vulnerable to physical shocks: pump station power outages, siltation canal breaches, intake contamination events, and severe drought restrictions.

The **Regional Water Distribution Network Resilience Intelligence** module enables operators and Water User Associations (WUAs) to run deterministic, non-destructive **"What-If" stress simulations** against the Yangiariq water conveyance grid.

### Key Architectural Tenet: Zero In-Place Mutability
Resilience scenario evaluations are executed **strictly in-memory** by `HydraulicResilienceEvaluator`.
- The baseline topology, live sensor telemetry, active seasonal water allocations, and database operational states remain completely untouched.
- Scenarios are saved as historical simulation records (`ResilienceScenario`) with detailed impact payloads, allowing operators to compare mitigation strategies side-by-side without risking production disruptions.

---

## 2. Supported Failure Scenarios

| Scenario Type | Simulation Trigger & Mechanics | Practical Application |
|---|---|---|
| `CANAL_BREACH` | Drops target canal capacity by specified percentage (up to 100% complete breach). Upstream head increases surge risk; downstream nodes lose conveyance. | Sudden levee erosion, bank sliding, or emergency canal maintenance shutoff. |
| `PUMP_FAILURE` | Zeroes discharge through associated pumping stations (e.g. Bogʻot Booster Pumping Station). Disables pressurized pipelines. | Electrical grid blackouts, motor burnout, or cavitation damage. |
| `INTAKE_CONTAMINATION` | Completely isolates the district intake point (e.g. Yangiariq Main Intake from Amu Darya branch). | Agricultural runoff spikes, toxic chemical spill upstream, or biosecurity alerts. |
| `DROUGHT_RESTRICTION` | Applies a uniform or sector-specific quota cut (e.g. 30–60% capacity curtailment). | Regional transboundary water treaty reductions or extreme dry-season rationing. |

---

## 3. Resilience Impact Calculation Engine

When an operator launches a simulation:

```
[SimulateResilienceScenarioUseCase]
           │
           ▼
[HydraulicResilienceEvaluator.evaluate(...)]
           │
           ├─► 1. Clone network topology graph in memory
           ├─► 2. Apply simulated capacity degradation (capacity * (1 - degradation%))
           ├─► 3. Trace reachability from primary intake nodes (BFS/DFS)
           ├─► 4. Identify downstream disconnected nodes & isolated inlets
           ├─► 5. Map affected agricultural parcels & aggregate unserved area (ha)
           ├─► 6. Sum planned irrigation volume stranded (m³)
           ├─► 7. Assess surge risks on immediate upstream canal segments
           └─► 8. Generate rule-based mitigation recommendations
```

### Metrics Produced:
- **`disconnectedNodesCount`**: Total junction, gate, and inlet nodes losing gravity/pressurized feed.
- **`affectedFieldCount`**: Count of agricultural parcels deprived of irrigation flow.
- **`unservedAreaHectares`**: Total cropland area at immediate drought/wilting risk.
- **`unservedVolumeM3`**: Sum of pending water requirements stranded upstream.
- **`vulnerableCrops`**: Breakdown of affected crops (Cotton, Wheat, Fruit Orchards, Rice).
- **`hydraulicSurgeRisks`**: Warning tags for upstream canal sections facing backwater surcharges due to sudden downstream gate or canal closures.

---

## 4. Explainable Mitigation Recommendations

For every evaluated scenario, the engine synthesizes operational guidance:
1. **Dynamic Rerouting**: Identifies candidate bypass channels or auxiliary secondary links capable of transferring flow around the breached segment.
2. **Deficit Sharing**: Calculates revised proportional allocation schedules across downstream WUAs to prevent localized catastrophic crop death.
3. **Emergency Surge Relief**: Highlights gates and spillway channels that must be opened upstream before closing gates, avoiding canal overtopping.
