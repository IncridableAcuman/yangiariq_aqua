# Water Network Topology & Capacity Engine

## 1. Physical Representation & Topology Model

The district conveyance infrastructure is modeled as a directed graph $G = (V, E)$:

### 1.1 Network Nodes ($V$)
Supported node types:
- `WATER_SOURCE`: Natural river intakes (Amu Darya) or regional reservoirs (Tuyamuyun).
- `RESERVOIR`: Storage basins providing buffered volume.
- `PUMP_STATION`: Electrified lift pumps lifting water into elevated conveyance tiers.
- `CANAL_JUNCTION`: Flow splitting and branch distribution points.
- `CONTROL_GATE`: Radial sluice gates, check gates, and division weirs.
- `DISTRIBUTION_POINT`: Secondary to tertiary distribution structures.
- `FIELD_INLET`: Parcel intake turnout connecting agricultural fields to secondary ditches or pressurized pipes.
- `TERMINAL_NODE`: Tail-end escapes, drainage collectors, and return-flow points.

### 1.2 Network Edges ($E$)
Supported conveyance edges:
- `PRIMARY_CANAL`: Main trunk channels (e.g., Polvon, Shovot) carrying multi-cubic-meter flows ($Q_{max} > 5.0\text{ m}^3/\text{s}$).
- `SECONDARY_CANAL`: District branch canals distributing water into WUAs ($Q_{max} \approx 1.5 - 4.0\text{ m}^3/\text{s}$).
- `DISTRIBUTION_CHANNEL`: Tertiary ditches directly serving field clusters.
- `PIPELINE`: Pressurized or gravity conduits supplying drip and sprinkler fields.

---

## 2. Deterministic Capacity Calculation

For each edge $e \in E$:

$$\text{Committed Flow } Q_{\text{committed}} = Q_{\text{current}} + Q_{\text{planned}}$$

$$\text{Remaining Capacity } Q_{\text{remaining}} = \max\left(0, Q_{\text{max}} - Q_{\text{committed}}\right)$$

$$\text{Utilization Percentage } U\% = \frac{Q_{\text{committed}}}{Q_{\text{max}}} \times 100\%$$

### Constraint States
- **SAFE**: $U\% < 90\%$ and no downstream active hydraulic safety interlocks.
- **CONSTRAINED**: $U\% \ge 90\%$, or edge is undergoing maintenance, or flow approaches maximum carrying capacity.
- **BLOCKED**: Active hydraulic interlock triggered (e.g., downstream suspected pipe burst or critical pressure drop), or edge is physically blocked.

---

## 3. Topology Validation (`NetworkTopologyValidator`)

The topology validator verifies the directed acyclic graph properties:
1. **Disconnected Nodes**: Ensures every node has at least one incoming or outgoing edge.
2. **Missing Inflow**: Ensures distribution points, junctions, pump stations, and field inlets have valid upstream supply edges.
3. **Source Reachability**: Verifies every active field inlet can trace a path back to at least one primary water source.
4. **Capacity Validity**: Verifies $Q_{max} > 0$ and $Q_{min} \ge 0$.
