# Analytics Metric Governance & Definitions

## 1. Governance Principles

To ensure complete trust and operational clarity across farmers, operators, and water managers, all metrics in **YANGIARIQ AQUA AI** follow strict governance:
1. **Unambiguous Definitions**: Each metric has exactly one formula and source.
2. **No Conflated Terminology**: `UTILIZATION`, `VARIANCE`, and `EFFICIENCY` are mathematically and conceptually distinct and must never be swapped.
3. **Data Quality Awareness**: Calculations explicitly distinguish `0` (measured zero), `null` (not measurable), and `UNKNOWN` (insufficient data).

---

## 2. Core Metrics Catalog

### 2.1 Water Quota Utilization
- **Business Meaning**: The percentage of the allocated seasonal water quota consumed to date.
- **Formula**:
  $$\text{Utilization} = \frac{\text{Total Consumed Volume } (m^3)}{\text{Total Allocated Volume } (m^3)} \times 100$$
- **Source**: `WaterConsumptionQueryRepository` aggregating `irrigation_events.actual_volume_m3` and `water_allocations.allocated_volume_m3`.
- **Classification**:
  - $\le 80\%$: `NORMAL`
  - $80.1\% - 100\%$: `WARNING`
  - $> 100\%$: `OVER_CONSUMED`

### 2.2 Planned vs Actual Water Variance
- **Business Meaning**: Deviation of delivered water from the smart irrigation plan recommendation.
- **Formulas**:
  $$\text{Variance Volume } (m^3) = \text{Actual Volume} - \text{Planned Volume}$$
  $$\text{Variance } (\%) = \frac{\text{Variance Volume}}{\text{Planned Volume}} \times 100$$
- **Interpretation Rule**:
  - Positive variance: Actual delivery exceeded planned recommendation.
  - Negative variance: Actual delivery was below planned recommendation (potential water saving or under-irrigation).
- **Distinction from Utilization**: A field may have low quota utilization (e.g., 40%) but high plan variance on an individual irrigation event (e.g., +25%).

### 2.3 Plan Adherence
- **Business Meaning**: The degree to which irrigation plans were executed as scheduled within acceptable variance bounds.
- **Formula**:
  $$\text{Plan Adherence } (\%) = \max\left(0, 100 - \min(40, |\text{Variance } \%|)\right)$$
  For individual field rankings:
  $$\text{Adherence Rate } (\%) = \min\left(100, \frac{\text{Completed Operations}}{\text{Total Scheduled Plans}} \times 100\right)$$

### 2.4 Operation Completion Rate
- **Business Meaning**: Proportion of planned operations that reached `COMPLETED` state vs `CANCELLED` or `BLOCKED`.
- **Formula**:
  $$\text{Completion Rate } (\%) = \frac{\text{Completed Operations}}{\text{Scheduled Plans} + \text{Completed Operations}} \times 100$$

### 2.5 Telemetry Coverage & Data Quality
- **Business Meaning**: Reliability of sensor telemetry readings during the analytical window.
- **Formula**:
  $$\text{Telemetry Coverage } (\%) = \frac{\text{Valid Readings (quality = 'VALID')}}{\text{Total Readings (VALID + SUSPECT + INVALID)}} \times 100$$
- **Data Confidence Levels**:
  - $\ge 85\%$: `COMPLETE`
  - $50\% - 84\%$: `PARTIAL` (triggers non-intrusive warning)
  - $< 50\%$: `INSUFFICIENT` (triggers high-visibility data quality warning)

### 2.6 Post-Irrigation Verification Coverage
- **Business Meaning**: Percentage of completed irrigation events verified by telemetry or agronomic observation.
- **Formula**:
  $$\text{Verification Coverage } (\%) = \frac{\text{Verified Operations}}{\text{Total Completed Operations}} \times 100$$
- **Strict Verification Criterion**:
  An event is verified only if:
  1. Status is `COMPLETED`, AND
  2. There exists at least one post-operation soil moisture reading on the parcel within 24 hours of execution, OR an explicit verification confirmation note.
  *Missing sensor data post-operation is NEVER treated as successful verification.*

---

## 3. Explainable Performance Index

| Factor | Weight | Scoring Curve |
| :--- | :---: | :--- |
| **Water Compliance** | $25\%$ | $100$ pts if utilization $\le 80\%$; linear drop to $50$ pts at $100\%$; linear drop to $0$ if $> 120\%$ |
| **Plan Adherence** | $25\%$ | Direct percentage of plan adherence ($0 - 100$) |
| **Telemetry Reliability** | $20\%$ | Direct percentage of telemetry coverage ($0 - 100$) |
| **Safety Record** | $20\%$ | $100$ pts (0 events), $50$ pts (warnings only), $0$ pts ($\ge 1$ critical anomaly) |
| **Verification Coverage**| $10\%$ | Direct percentage of verified completed operations ($0 - 100$) |

### Status Classification Rules
- `ATTENTION`: Triggered if $\ge 1$ critical anomaly OR water utilization $> 100\%$.
- `WARNING`: Triggered if warnings exist OR utilization $> 80\%$ OR composite score $< 65$.
- `NORMAL`: All parameters compliant.
