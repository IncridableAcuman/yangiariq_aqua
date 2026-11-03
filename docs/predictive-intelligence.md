# Predictive Farm Intelligence & Forecasting (Milestone 08.7)

## 1. Executive Summary & Objective

**Milestone 08.7** elevates the **YANGIARIQ AQUA AI** platform from retrospective performance auditing into forward-looking **Predictive Decision Support**.

While previous milestones established:
- *"What is happening now?"* (Sensors, Telemetry, Anomalies — Milestones 08.1–08.3)
- *"What should I do now?"* (Smart Irrigation, Agronomic Recommendations, Operations Center — Milestones 08.4–08.5)
- *"What happened before?"* (Historical Analytics, Trends, Calibration — Milestone 08.6)

Milestone 08.7 addresses the critical operational question:
> **"What is likely to happen next?"**

The predictive intelligence pipeline operates strictly under the principle:
```
DATA & TELEMETRY → METRICS & REGRESSION → FORECAST → CONFIDENCE & UNCERTAINTY → EVIDENCE & ASSUMPTIONS → RECOMMENDED ACTION
```

---

## 2. Critical Safety Principles & Boundaries

### 2.1 Pure Decision Support — Zero Autonomous Execution
The predictive system is strictly **advisory**. Under no circumstance will the prediction engine:
- Automatically open or close valves
- Automatically start or stop pumps
- Automatically modify water allocations or quotas
- Automatically approve smart irrigation plans
- Override hydraulic safety interlocks

Every action recommendation requires explicit operator or farmer review, authorization, and dispatch through established operational workflows.

### 2.2 Safety Dominance & Hydraulic Interlock Integrity
If a critical hydraulic anomaly is active (e.g. `HIGH_PRESSURE_SURGE`, `PIPE_BURST`, `CANAL_OVERFLOW`, or `VALVE_FAULT`), the system assigns the immutable risk status:
$$\text{RiskLevel} = \mathbf{BLOCKED}$$
This status:
- Overrides all agronomic moisture deficits
- Displays persistent high-visibility warnings
- Prohibits automated plan creation suggestions until cleared in the Irrigation Operations Center

---

## 3. Supported Prediction Horizons

The platform models three standardized operational horizons:

| Horizon | Granularity | Agronomic Purpose |
| :--- | :--- | :--- |
| `NEXT_24_HOURS` | Hourly to sub-daily | Immediate operational shift planning, nightly pump scheduling, valve readiness. |
| `NEXT_48_HOURS` | 12-hour steps | Primary tactical decision window, water allocation requests, labor dispatching. |
| `NEXT_7_DAYS` | Daily aggregation | Weekly water budget scheduling, regional canal intake coordination. |

---

## 4. The 6 Core Prediction Capabilities

1. **Soil Moisture Trajectory (`SOIL_MOISTURE_FORECAST`)**:
   - Computes expected root-zone volumetric moisture (%) using robust linear regression of recent observations bounded by FAO-56 daily crop evapotranspiration ($ET_c$).
   - Computes uncertainty intervals $[\text{lowerBound}, \text{upperBound}]$.
2. **Irrigation Urgency & Likelihood (`IRRIGATION_NEED_FORECAST`)**:
   - Assesses when projected soil moisture breaches the crop-specific Management Allowed Depletion (MAD) threshold.
   - Categorizes likelihood: `NOT_EXPECTED`, `LOW_LIKELIHOOD`, `POSSIBLE`, `LIKELY`, `HIGHLY_LIKELY`.
3. **Volumetric Water Demand (`WATER_DEMAND_FORECAST`)**:
   - Forecasts total expected water requirements ($m^3$) per parcel and farm-wide based on approved plans and agronomic deficits to field capacity ($FC$).
4. **Water Quota Headroom Risk (`WATER_AVAILABILITY_RISK`)**:
   - Compares expected cumulative demand against remaining seasonal allocation.
   - Detects quota exhaustion before field stress occurs.
5. **Irrigation Plan & Safety Risk (`IRRIGATION_PLAN_RISK`)**:
   - Validates scheduled irrigation runs against canal capacity, quota sufficiency, telemetry freshness, and active hydraulic anomalies.
6. **Sensor Telemetry Reliability (`SENSOR_RELIABILITY_FORECAST`)**:
   - Analyzes sensor battery health, transmission jitter, and stale-interval frequency over a 14-day window to preemptively flag failing telemetry nodes.

---

## 5. Explainable AI (XAI) Modal Structure

Every forecast item in the UI includes a deterministic breakdown with 5 standardized sections:
1. **WHAT (Forecast State)**: Projected numerical metric, target timestamp, and uncertainty intervals.
2. **WHY (Agronomic & Hydraulic Rationale)**: Underlying physical reasoning (e.g. soil depletion velocity, crop phenology stage, hydraulic constraints).
3. **EVIDENCE (Quantitative Data)**: Exact regression slope (%/day), observation count, telemetry age, and active anomalies.
4. **ASSUMPTIONS & LIMITATIONS**: Atmospheric assumptions (FAO-56 standard $ET_c$, zero unexpected rainfall, constant canal delivery pressure).
5. **RECOMMENDED NEXT STEP**: Clear operational CTA linking directly to `/irrigation`, `/water`, or `/operations`.

---

## 6. Frontend Workspace (`/predictions`)

The predictive workspace provides:
- **Horizon Selector**: Instant toggling between 24h, 48h, and 7d horizons.
- **Farm Overview Strip**: 5 KPI signals (urgent parcels, total projected $m^3$, quota risks, sensor health %, active hydraulic blocks).
- **Parcel Forecast Panel**: Detailed single-parcel view with projected moisture curves, water demand, and risk overview.
- **Irrigation Need Ranking Matrix**: Interactive, searchable table ranking fields by depletion urgency.
- **Calibration History & Accuracy Table**: Empirical tracking of past predictions versus ground truth measurements (accuracy status: `VERIFIED_ACCURATE`, `VERIFIED_INACCURATE`, `PENDING`, `EXPIRED`, `UNVERIFIABLE`).

---

## 7. Model Evaluation, Accuracy & Continuous Intelligence (Milestone 08.8)

Milestone 08.8 adds a dedicated evaluation and continuous intelligence loop to the predictive intelligence platform:
- **Core Loop**: `Prediction → Outcome → Matching → Evaluation → Error → Classification → Insight → Calibration`.
- **Honest Sample Gating**: If evaluated samples $n < 10$, all aggregate metrics are suppressed with a prominent notice: *"Insufficient data for evaluation ($n = X$, minimum required: 10)"*. Never fabricates accuracy.
- **Dual Metric Separation**:
  - Regression errors: MAE, RMSE, safe MAPE (safely handling zero denominators), Mean Bias Error (MBE), and uncertainty interval tolerance hit rate.
  - Classification metrics: Confusion matrix (TP, TN, FP, FN, Interrupted by Safety), Accuracy, Precision, Recall, and F1 score.
- **Safety Interlock Preservation**: Planned irrigations halted by hydraulic safety interlocks are classified as `OUTCOME_INTERRUPTED` rather than false positives, honoring physical system integrity.
- **Continuous Intelligence & Drift Alerts**: Automatically compares recent 7-day error rates against historical 30-day baselines, raising a performance drift warning if error exceeds baseline by $\ge 30\%$.
- **Human-in-the-Loop Audit**: Audits the alignment between Forecast Recommendation, Human Decision (approved vs modified vs rejected), and Operational Reality.

---

## 8. Adaptive Intelligence, Model Governance & Controlled Calibration (Milestone 08.9)

Milestone 08.9 extends evaluation insights into an end-to-end governed calibration lifecycle:
$$\text{Insight} \longrightarrow \text{Calibration Proposal} \longrightarrow \text{Historical Backtest} \longrightarrow \text{Human Review} \longrightarrow \text{Version} \longrightarrow \text{Controlled Rollout} \longrightarrow \text{Monitoring} \longrightarrow \text{Rollback}$$

- **Zero Autonomous Self-Modification**: The platform strictly prohibits automatic changes to formulas, $K_c$, $ET_0$, or thresholds without explicit human administrator approval (`ROLE_ADMIN`).
- **Hard Safety Constraint Sovereignty**: Hard hydraulic safety parameters (`pipeBurstPressureBar`, `emergencyPressureThreshold`, `hydraulicSafetyInterlock`, etc.) are permanently immutable and blocked from calibration.
- **Deterministic Anti-Leakage Backtesting**: Evaluates candidate parameter sets on historical ground truth using identical closed evaluation windows and strictly preventing access to post-prediction observations.
- **Multi-Factor Regression Guard**: Blocks approval if improving one metric (e.g., MAE) degrades another (e.g., false positives or RMSE).
- **Controlled Rollout Lifecycle**: Safe staged deployment (`DRAFT` $\to$ `CANDIDATE` $\to$ `APPROVED` $\to$ `SHADOW` $\to$ `CANARY` $\to$ `ACTIVE`).
- **Emergency 1-Click Rollback**: Allows immediate atomic rollback to any previous immutable version with mandatory justification.
- **Immutable Audit Trail**: Chronologically logs all proposal creation, backtest execution, human review decisions, activations, and rollbacks.
