# Farm Performance, Historical Intelligence & Analytics (Milestone 08.6)

## 1. Executive Overview

The **YANGIARIQ AQUA AI** platform previously answered operational real-time questions:
- *"What is happening now?"* (Sensors, telemetry, anomalies)
- *"What should I do now?"* (Agronomic recommendations, smart irrigation plans, Action Center)

**Milestone 08.6** closes the operational loop by establishing historical intelligence and agricultural performance auditing:
- *"What happened before?"*
- *"What changed?"*
- *"How well did we perform?"*
- *"What patterns can we see?"*
- *"What should we improve?"*

This capability transforms raw time-series measurements and manual/automated execution records into an actionable intelligence pipeline:
```
OPERATIONAL DATA → METRICS → CHRONOLOGICAL TRENDS → COMPARISONS → DETERMINISTIC INSIGHTS → ACTIONABLE WORKFLOWS
```

---

## 2. Product Principles & Anti-Patterns

1. **Agricultural Relevance First**: This is not a generic BI dashboard filled with decorative graphs and vanity metrics. Every metric directly answers a critical agricultural or hydraulic question.
2. **Deterministic Rules Over "Fake AI"**: Insights are produced by transparent rule-based evaluators using actual data thresholds. No synthetic "AI predictions" are generated without physical evidence.
3. **Safety Dominance**: Operational safety is absolute. No composite score, high water compliance, or verified operation can mask or override a critical hydraulic hazard.
4. **Data Confidence Transparency**: The system distinguishes `COMPLETE`, `PARTIAL`, and `INSUFFICIENT` data. Missing sensor telemetry is never assumed to indicate successful irrigation.
5. **Zero Dead-Ends**: Every insight and problematic parcel deep-links directly into existing execution workflows (water allocations, sensors, operations center, or anomalies).

---

## 3. Workspace Architecture (`/analytics`)

The Farm Performance workspace is accessible via the primary navigation at `/analytics`. It contains structured analytical sections:

```
┌────────────────────────────────────────────────────────────────────────┐
│ Period Filter Bar (Today | 7d | 30d | 90d | Season | Custom) + Parcel   │
├────────────────────────────────────────────────────────────────────────┤
│ Farm Overview Strip (Fields, Cultivated Area, Water Quota, Ops, Sensors)│
├────────────────────────────────────────────────────────────────────────┤
│ 5 Core Performance KPIs (Utilization, Adherence, Telemetry, Verif, Safe)│
├──────────────────────────────────┬─────────────────────────────────────┤
│ Water Quota & Variance Card      │ Period-over-Period Comparison Card  │
├──────────────────────────────────┼─────────────────────────────────────┤
│ Irrigation Execution Discip.     │ Sensor Quality & Confidence Card    │
├──────────────────────────────────┴─────────────────────────────────────┤
│ Chronological Trend Charts (Actual vs Planned Water, Ops & Telemetry)  │
├────────────────────────────────────────────────────────────────────────┤
│ Actionable Farm Insights (WHAT, WHY, EVIDENCE, NEXT STEP)              │
├────────────────────────────────────────────────────────────────────────┤
│ Field Performance & Explainable Ranking Table (Paginated, Sortable)    │
└────────────────────────────────────────────────────────────────────────┘
```

### Temporal Period Presets
| Preset | Definition | Default Granularity |
| :--- | :--- | :--- |
| `TODAY` | UTC midnight to current timestamp | Hourly / Daily |
| `LAST_7_DAYS` | Last 7 full calendar days | Daily |
| `LAST_30_DAYS` | Standard evaluation window (last 30 days) | Daily |
| `LAST_90_DAYS` | Quarterly agricultural stage window | Weekly |
| `SEASON` | From April 1st of current year (or March) to now | Weekly / Monthly |
| `CUSTOM` | User-defined bounds (validated: `start <= end`, max 365 days) | Automatic |

---

## 4. Authoritative Performance Metrics

### 4.1 Water Quota Utilization vs Plan Variance
Metric definitions maintain strict domain governance and are never conflated:
- **Water Quota Utilization (%)**:
  $$\text{Utilization} = \frac{\text{Total Consumed Volume } (m^3)}{\text{Total Allocated Seasonal Quota } (m^3)} \times 100$$
  - `NORMAL`: $\le 80\%$
  - `WARNING`: $80\% < \text{Utilization} \le 100\%$
  - `OVER_CONSUMED`: $> 100\%$ (triggers mandatory high-priority insight)
- **Plan Variance ($m^3$ and %)**:
  $$\text{Variance Volume} = \text{Actual Delivered Volume} - \text{Recommended Planned Volume}$$
  $$\text{Variance } \% = \frac{\text{Variance Volume}}{\text{Planned Volume}} \times 100$$
  - Contextual interpretation is provided (e.g. negative variance may indicate water savings or under-irrigation).
- **Quota Remaining ($m^3$)**:
  $$\text{Remaining} = \max(0, \text{Allocated Quota} - \text{Consumed Volume})$$

### 4.2 Irrigation Operations Performance & Plan Adherence
- **Plan Adherence (%)**: Evaluates operational discipline—the percentage of completed operations executed within acceptable volume and duration variance ($\pm 20\%$).
- **Operation Completion Rate (%)**: Completed operations divided by scheduled operations.
- **Duration Metrics**: Average planned duration vs average actual duration (in minutes).

### 4.3 Sensor Telemetry Quality & Availability
- **Telemetry Coverage (%)**:
  $$\text{Telemetry Coverage} = \frac{\text{Valid Sensor Readings}}{\text{Total Sensor Readings Received}} \times 100$$
- **Sensor Fleet Availability (%)**:
  $$\text{Availability} = \frac{\text{Active Sensors}}{\text{Total Sensors Installed}} \times 100$$

### 4.4 Post-Irrigation Verification
- **Verification Coverage (%)**:
  $$\text{Verification Coverage} = \frac{\text{Verified Operations}}{\text{Total Completed Operations}} \times 100$$
- Strict Rule: Incomplete or missing telemetry post-operation results in `UNVERIFIED` status, never assumed success.

---

## 5. Explainable Field Performance Index

Each agricultural parcel receives an explainable score between $0$ and $100$:

$$\text{Score} = (W \times 0.25) + (A \times 0.25) + (T \times 0.20) + (S \times 0.20) + (V \times 0.10)$$

Where:
1. $W$ = Water Quota Compliance Factor ($25\%$)
2. $A$ = Plan Adherence Factor ($25\%$)
3. $T$ = Telemetry Reliability Factor ($20\%$)
4. $S$ = Safety Record Factor ($20\%$)
5. $V$ = Verification Coverage Factor ($10\%$)

### Safety Dominance Invariant
If a field has **$\ge 1$ critical hydraulic anomaly** or **$> 100\%$ water quota utilization**, its status is immediately forced to `ATTENTION`, regardless of numerical score.

---

## 6. Deterministic Farm Insight Engine

The engine evaluates parcels and emits prioritized, actionable conclusions:

| Trigger | Category | Severity | Actionable Next Step | Workflow Target |
| :--- | :--- | :--- | :--- | :--- |
| $\ge 2$ Critical hydraulic anomalies | `SAFETY` | `CRITICAL` | Inspect pipeline & hydraulic valves | `/anomalies` |
| Quota utilization $> 100\%$ | `WATER` | `HIGH` | Re-evaluate water quota allocation | `/water` |
| Telemetry coverage $< 70\%$ | `TELEMETRY` | `WARNING` | Check sensor hardware & connectivity | `/sensors` |
| Verification coverage $< 75\%$ | `VERIFICATION` | `WARNING` | Review unverified irrigation events | `/operations` |
| Plan adherence $< 70\%$ | `IRRIGATION` | `WARNING` | Audit operational execution windows | `/operations` |

---

## 7. Security & Multi-Tenancy

- **FARMER**: Confined strictly to parcels owned by the authenticated farmer via `FieldOwnershipChecker`. Cross-tenant parcel requests return `404 NOT FOUND`.
- **OPERATOR**: District-wide aggregation across Yangiariq canals, pumping stations, and collective parcels.
- **ADMIN**: Global platform scope.
