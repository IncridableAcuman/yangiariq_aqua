# Milestone 08.7 Completion Report: Predictive Farm Intelligence & Forecasting

## 1. Executive Summary

Milestone 08.7 successfully elevates the **YANGIARIQ AQUA AI** platform from historical performance intelligence into forward-looking **Predictive Decision Support**. The capability enables farmers, water dispatchers, and district agronomists to preemptively identify soil moisture depletion, schedule water orders before stress occurs, forecast volumetric irrigation demands, detect quota depletion risks, enforce hydraulic safety locks, and monitor telemetry device reliability across three standardized horizons (`NEXT_24_HOURS`, `NEXT_48_HOURS`, `NEXT_7_DAYS`).

All work was designed and implemented strictly under the non-negotiable architectural invariant: **Decision Support Only — Zero Autonomous Control**. Predictions cannot actuate valves, toggle pumps, or mutate allocations.

---

## 2. Core Architectural Philosophy

1. **Deterministic Foundations First**: No synthetic ML hallucination, cloud API dependency, or opaque black-box models. All projections stem from physics-informed, FAO-56 agronomical formulas and robust linear regressions.
2. **Explicit Uncertainty**: When telemetry is sparse or stale, the system explicitly declares `UNKNOWN` or `INSUFFICIENT` states rather than fabricating values.
3. **Safety Dominance**: Hydraulic anomaly interlocks (`PIPE_BURST`, `HIGH_PRESSURE_SURGE`, `CANAL_OVERFLOW`, `VALVE_FAULT`) strictly override moisture urgency, yielding immutable `BLOCKED` status.
4. **Hexagonal Modularity**: Packaged in `com.aquaai.prediction`, fully governed by 72 ArchUnit rules prohibiting cross-boundary contamination.

---

## 3. Predictive Domain Model

Located in `com.aquaai.prediction.domain.model`:
- **`Prediction`**: Immutable aggregate root capturing field ID, prediction type, horizon, target timestamp, predicted value, lower/upper uncertainty bounds, confidence score, risk level, data sufficiency, likelihood, and structured XAI evidence.
- **`PredictionType`**:
  - `SOIL_MOISTURE_FORECAST`
  - `IRRIGATION_NEED_FORECAST`
  - `WATER_DEMAND_FORECAST`
  - `WATER_AVAILABILITY_RISK`
  - `IRRIGATION_PLAN_RISK`
  - `SENSOR_RELIABILITY_FORECAST`
- **`PredictionHorizon`**: `NEXT_24_HOURS`, `NEXT_48_HOURS`, `NEXT_7_DAYS`.
- **`RiskLevel`**: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `BLOCKED`, `UNKNOWN`.
- **`DataSufficiency`**: `SUFFICIENT`, `LIMITED`, `INSUFFICIENT`.
- **`IrrigationLikelihood`**: `NOT_EXPECTED`, `LOW_LIKELIHOOD`, `POSSIBLE`, `LIKELY`, `HIGHLY_LIKELY`, `UNKNOWN`.
- **`PredictionAccuracyStatus`**: `PENDING`, `VERIFIED_ACCURATE`, `VERIFIED_INACCURATE`, `EXPIRED`, `UNVERIFIABLE`.

---

## 4. Model Provider Abstraction

- **`PredictionProvider` (Domain Port)**: Declares SPI contract for evaluating multi-horizon field predictions without coupling domain models to specific algorithm implementations.
- **`DeterministicPredictionProvider` (Infrastructure Provider)**: Production implementation in `com.aquaai.prediction.infrastructure.provider` (satisfying ArchUnit Rule 3). Orchestrates domain services:
  - `SoilMoistureForecaster`
  - `IrrigationNeedForecaster`
  - `WaterDemandForecaster`
  - `WaterAvailabilityForecaster`
  - `IrrigationPlanRiskForecaster`
  - `SensorReliabilityForecaster`
  - `DataSufficiencyEvaluator`

---

## 5. Deterministic Forecasting Algorithms

1. **Least-Squares Slope Estimation**: Analyzes calibrated telemetry points over a 48h window to establish baseline velocity (%/hour).
2. **FAO-56 Evapotranspiration Clamping**: Constrains regression slope against crop-specific daily evapotranspiration ($ET_c = K_c \times ET_0$) to prevent runaway negative or positive rates.
3. **Volumetric Restitution**: Computes exact replenishment volume ($m^3$) restoring root-zone moisture from projected levels to Field Capacity ($\text{FC}$), factoring application efficiency $\eta$.
4. **Uncertainty Spreads**: Evaluates variance over time, scaling spread width dynamically with horizon duration ($\Delta t$).

---

## 6. Data Sufficiency & Missing Telemetry Strategy

- **`SUFFICIENT`**: $\ge 6$ readings in past 48h, latest $\le 12$h old, sensor status `ONLINE`. Narrow uncertainty bounds ($\pm 2.5\%$).
- **`LIMITED`**: $3$–$5$ readings in past 48h, latest $12$–$24$h old. Wider uncertainty bounds ($\pm 5.0\%$) and confidence penalties.
- **`INSUFFICIENT`**: $< 3$ readings or latest $> 24$h old or sensor `OFFLINE`. **Zero numeric prediction generated**; status set to `UNKNOWN` with explicit warning to inspect hardware.

---

## 7. Horizon Architecture (24h, 48h, 7d)

- **`NEXT_24_HOURS`**: Shift-level operational tactical decisions (valve scheduling, nocturnal pumping).
- **`NEXT_48_HOURS`**: Primary operational decision window (smart irrigation plan dispatch, allocation requests).
- **`NEXT_7_DAYS`**: Strategic weekly water balance planning (canal intake negotiation, district quota management).

---

## 8. Soil Moisture Depletion Modeling

Models root-zone depletion trajectory:
$$\theta_{\text{projected}} = \theta_{\text{latest}} + (v_{\text{eff}} \times \Delta t)$$
Bounded by Wilting Point ($\text{WP}$) and Field Capacity ($\text{FC}$), with dynamic uncertainty envelopes.

---

## 9. Volumetric Water Demand Forecasting

Aggregates approved upcoming irrigation plan volumes and projected agronomic deficits across parcels, computing farm-level expected demand ($m^3$) with lower and upper confidence bounds.

---

## 10. Water Quota Risk & Headroom Evaluation

Evaluates ratio of remaining seasonal quota to projected horizon demand:
- Headroom $< 1.0 \implies \mathbf{CRITICAL}$ (imminent allocation exhaustion).
- Headroom $1.0$–$1.3 \implies \mathbf{HIGH}$ (tight headroom).
- Headroom $\ge 2.0 \implies \mathbf{LOW}$ (adequate reserve).

---

## 11. Hydraulic Safety Interlock & Plan Risk

Monitors open hydraulic safety interlocks. When active anomalies (`HIGH_PRESSURE_SURGE`, `PIPE_BURST`, `CANAL_OVERFLOW`, `VALVE_FAULT`) are detected on a canal or pipeline section:
- Assigns $\mathbf{BLOCKED}$ risk level.
- Prohibits automated scheduling.
- Deep-links to Operations Center for mandatory fault clearance.

---

## 12. Sensor Reliability Forecasting

Examines 14-day telemetry intervals, stale-transmission frequencies, and hardware availability status:
$$\text{Reliability } \% = 100 \times \left(1.0 - \left(0.4 \times \frac{\text{stale}}{\text{expected}} + 0.4 \times \frac{\text{offline}}{\text{total}} + 0.2 \times \text{jitter}\right)\right)$$
Preemptively alerts operators before hardware failure causes data starvation.

---

## 13. Explainable AI (XAI) Modal Structure

Every prediction card exposes a modal with 5 structured sections:
1. **WHAT**: Forecasted value, target timestamp, and uncertainty intervals.
2. **WHY**: Agronomic and hydraulic physical rationale.
3. **EVIDENCE**: Quantitative observations, regression velocity, telemetry age, and active anomalies.
4. **ASSUMPTIONS & LIMITATIONS**: FAO-56 atmospheric parameters, zero unexpected precipitation, constant delivery head.
5. **RECOMMENDED NEXT STEP**: Concrete operational action with deep-links to `/irrigation`, `/water`, or `/operations`.

---

## 14. Decision Support vs Autonomous Control Boundaries

- Zero automatic valve opening or closing.
- Zero autonomous pump starting or stopping.
- Zero automatic quota mutations.
- Zero automatic plan approvals.
- Human operator authorization required for all operational transitions.

---

## 15. Backend Hexagonal Implementation

- **Domain Package (`com.aquaai.prediction.domain`)**: `model`, `port`, `service`.
- **Application Package (`com.aquaai.prediction.application`)**: `usecase`, `dto`.
- **Infrastructure Package (`com.aquaai.prediction.infrastructure`)**: `adapter`, `persistence`, `provider`.
- **API Package (`com.aquaai.prediction.api`)**: `PredictionController` under `/api/v1/predictions/*`.

---

## 16. Database Schema & Liquibase Migrations

- Created `backend/src/main/resources/db/changelog/changes/015-create-prediction-domain-tables.sql`.
- Registered changeSet `015` in `db.changelog-master.xml`.
- Created table `predictions` with B-tree indices on `(field_id, horizon)`, `(target_time)`, and `(prediction_type, accuracy_status)`.

---

## 17. High-Performance Batch Data Access & Projections

`PredictionDataProviderImpl` executes indexed JDBC batch queries:
- Reuses authoritative `WaterConsumptionQueryRepository.getFieldWaterSummaries(...)`.
- Resolves parcel metadata, sensor health, and telemetry windows without N+1 query cascades.
- Escapes reserved keywords (`r."value"`).

---

## 18. Security Architecture & Anti-IDOR Isolation

- **Owner-Tenant Scoping**: Farmers can only access parcels they own.
- **Anti-Enumeration 404**: Unauthorized parcel access returns `404 NOT_FOUND` instead of `403 FORBIDDEN`.
- **District Operator & Admin Roles**: Multi-parcel canal scope and global read privileges verified.

---

## 19. ArchUnit Architecture Governance (Rules 66–72)

Added 7 new architecture rules to `ArchitectureTests.java`:
- Rule 66: Hexagonal package containment for `com.aquaai.prediction`.
- Rule 67: Domain isolation (zero dependencies on application, infrastructure, API).
- Rule 68: Application use case isolation.
- Rule 69: Infrastructure provider package placement.
- Rule 70: API controller naming and location.
- Rule 71: External bounded context isolation.
- Rule 72: Domain repository interface location.
All 72 rules pass.

---

## 20. Frontend FSD Architecture & Component Breakdown

- `entities/prediction`: `types.ts`, `api.ts`, `index.ts`.
- `features/prediction`:
  - `queries/usePredictionQueries.ts`
  - `PredictionHorizonSelector.tsx`
  - `DataSufficiencyBadge.tsx`
  - `SoilMoistureForecastCard.tsx`
  - `WaterDemandForecastCard.tsx`
  - `PredictionRiskOverview.tsx`
  - `IrrigationNeedMatrix.tsx`
  - `ForecastHistoryTable.tsx`
  - `PredictionExplanationModal.tsx`
  - `PredictiveSummaryCard.tsx`
- `pages/predictions/PredictionsPage.tsx`.

---

## 21. Multi-Horizon User Experience & State Management

- Interactive horizon switcher for 24h, 48h, and 7d.
- TanStack Query server-state caching with automated invalidation.
- Zero client-side state duplication.

---

## 22. GIS Integration & Deep-Linking

- Integrated into `FieldWaterSidePanel.tsx`: Added `[View Predictive Forecasts →]` action button.
- `GisPage.tsx` handles navigation to `/predictions?fieldId=...`.

---

## 23. Farm Intelligence Dashboard Summary Card Integration

- `PredictiveSummaryCard` integrated into `DashboardPage.tsx`.
- Displays 4 real-time forecast signals: Irrigation Need Count, Projected Demand $m^3$, Quota Risks, Sensor Reliability %.
- Renders active hydraulic lock alerts and 1-click CTA `[Barcha prognozlarni ko‘rish →]`.

---

## 24. Historical Calibration & Forecast Accuracy Tracking

- `ForecastHistoryTable` compares past predictions against actual sensor readings upon target timestamp arrival.
- Statuses: `VERIFIED_ACCURATE`, `VERIFIED_INACCURATE`, `PENDING`, `EXPIRED`, `UNVERIFIABLE`.
- Zero synthetic accuracy scores: Displays explicit fallback notice when sample size is insufficient.

---

## 25. Trilingual Internationalization (UZ, RU, EN) & Dark Mode

- Fully localized across Uzbek (`uz`), Russian (`ru`), and English (`en`).
- CSS variables and dark mode compatibility verified across all components.

---

## 26. Verification Results

| Test Category | Suite / Tool | Tests Count | Status |
| :--- | :--- | :--- | :--- |
| **Backend Tests** | Gradle / JUnit 5 | **565 tests** | **0 failures (100% PASS)** |
| **ArchUnit Architecture** | ArchUnit 1.3.0 | **72 rules** | **0 violations (100% PASS)** |
| **Frontend Tests** | Vitest 2.1.9 | **174 tests (20 suites)** | **0 failures (100% PASS)** |
| **Frontend Typecheck** | TypeScript 5.6 (`tsc -b`) | Entire project | **0 errors** |
| **Frontend Linter** | ESLint 9 | Entire project | **0 errors, 0 warnings** |
| **Frontend Production Build** | Vite 5.4.21 | Production bundle | **SUCCESS** |

---

## 27. Architectural Invariant Compliance

- Single deployable unit, strictly decoupled.
- Zero external ML framework dependencies.
- No duplicate calculation engines.
- Safety interlock absolute precedence maintained.
- Anti-IDOR tenant protection verified.

---

## 28. Transition to Operational Readiness & Explicit Boundary

Milestone 08.7 is complete, verified, and operational.
**Milestone 08.8 has NOT been started**, adhering strictly to instructions.
