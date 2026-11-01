# Milestone 08.3 Completion Report: Telemetry Intelligence, Anomaly Detection & Agronomic Recommendations

## 1. Executive Summary
Milestone 08.3 has been successfully implemented and verified for the Yangiariq Aqua AI platform. This milestone establishes a cohesive, deterministic intelligence pipeline above the canonical sensor telemetry stream:

$$\text{SensorReading} \longrightarrow \text{Telemetry Normalization} \longrightarrow \text{Trend Evaluation} \longrightarrow \text{Anomaly Detection} \longrightarrow \text{Agronomic Assessment} \longrightarrow \text{Water Budget \& Safety Check} \longrightarrow \text{Decision-Support Recommendation}$$

The system operates strictly as **DECISION SUPPORT ONLY**. Direct physical control of irrigation equipment (valves, pumps, actuators, or IoT controllers) is **prohibited**. AI is utilized solely for non-authoritative advisory context, while deterministic physics and agronomy engines compute all authoritative metrics.

---

## 2. Existing Intelligence Audit
Before Milestone 08.3, the repository contained:
- **Milestone 07.1 & 07.3 (`com.aquaai.sensor`)**:
  - `Sensor` aggregate root with operational states (`ACTIVE`, `INACTIVE`, `MAINTENANCE`, `OFFLINE`).
  - `SensorReading` time-series observations with reading quality (`VALID`, `SUSPECT`, `INVALID`).
  - B-tree indexing and temporal bucketing (`5m` to `1w`) in PostgreSQL.
- **Milestone 08.0 (`com.aquaai.anomaly`)**:
  - `Anomaly` aggregate root and configurable rules (`SoilMoistureRuleDetector`, `WaterFlowRuleDetector`, `WaterPressureRuleDetector`, `IrrigationFailureDetector`, `SensorHealthDetector`).
  - Active deduplication partial index in PostgreSQL.
- **Milestone 08.1 (`com.aquaai.recommendation`)**:
  - `AgronomicRecommendation` aggregate root with corroborating evidence and recommended action lists.
  - `DeterministicAgronomicEngineImpl` for agricultural rules and `CompositeAgronomicAIProvider` with Gemini / Mock fallback.
- **Milestone 08.2 (`com.aquaai.irrigation`)**:
  - `SmartIrrigationPlan` aggregate with 8-state lifecycle (`DRAFT`, `PROPOSED`, `APPROVED`, `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `EXPIRED`).
  - `HydraulicSafetyInterlock` enforcing physical safety blocks on pipe bursts and pressure drops.
  - `WaterBudget` tracking allocated, consumed, committed, and remaining water volume.

**What was missing prior to 08.3**:
- A unified bounded context (`com.aquaai.intelligence`) orchestrating the transformation of raw sensor readings into normalized signals, rate-of-change temporal trends ($\Delta\text{moisture}/\Delta t$), and engineering SLA freshness policies (`FRESH`, `STALE`, `MISSING`).
- Explicit `AgronomicAssessment` aggregate persisting parcel-level interpretation with supporting signals, hydraulic lock status, and water budget constraints.
- Formal decoupling between recommendations and smart plans: recommendations are advisory hypotheses, whereas plans require explicit human approval before execution.

---

## 3. Domain Model
The `com.aquaai.intelligence.domain` bounded context models:
- **`TelemetrySignal`**: Normalized observation capturing `fieldId`, `sensorId`, `SensorType`, `value`, `MeasurementUnit`, `ReadingQuality`, `measuredAt`, and `TelemetryFreshness`.
- **`TelemetryFreshness`**: Enum (`FRESH`, `STALE`, `MISSING`) governed by modality-specific SLA windows.
- **`TelemetryTrend`**: Temporal trend value object calculating directional trend (`INCREASING`, `DECREASING`, `STABLE`) and rate of change per hour ($\Delta v / \Delta t$) via `BigDecimal`.
- **`AgronomicSignalType`**: Standardized signal types (`SOIL_TOO_DRY`, `SOIL_TOO_WET`, `HIGH_TEMPERATURE`, `LOW_TEMPERATURE`, `HIGH_HUMIDITY`, `LOW_HUMIDITY`, `LOW_WATER_LEVEL`, `ABNORMAL_FLOW`, `PRESSURE_DROP`, `NO_RECENT_TELEMETRY`).
- **`AgronomicSignal`**: Individual supporting signal with `observedValue`, `thresholdValue`, severity, and human-readable explanation.
- **`AgronomicAssessment`**: Aggregate Root capturing primary signal, overall severity, moisture trend, freshness, bounded confidence score, hydraulic safety block status, and water budget constraint status.
- **`IntelligenceRecommendation`**: Advisory recommendation entity indicating suggested action, priority, reason, recommended volume ($\text{m}^3$), duration (minutes), window, and constraints.
- **`IntelligenceSeverity`**: `INFO`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- **`IntelligenceConfidence`**: Transparent confidence rating (`HIGH`, `MEDIUM`, `LOW`) derived from telemetry quality, freshness, and signal coherence.

---

## 4. Anomaly Detection Rules
The system enforces deterministic detection across sensor modalities:
1. **Soil Moisture Deficit & Critical Drought**: Evaluates volumetric moisture against root zone wilting thresholds ($< 15.0\%$ triggers `CRITICAL` drought alert; $15.0\% - 20.0\%$ triggers `HIGH` deficit alert).
2. **Soil Saturation & Root Hypoxia**: Moisture $> 35.0\%$ triggers `SOIL_TOO_WET` warning to prevent root asphyxiation.
3. **Hydraulic Pipe Burst Corroboration**: Detects sudden flow surge coupled with pressure drop, immediately activating `HydraulicSafetyInterlock` and generating `CRITICAL` `ABNORMAL_FLOW`.
4. **Hydraulic Mainline Pressure Drop**: Line pressure $< 1.2\text{ bar}$ triggers `PRESSURE_DROP` `CRITICAL` warning.
5. **Atmospheric Heat Stress (Garmsel)**: Ambient temperature $> 40.0^\circ\text{C}$ or relative humidity $< 18.0\%$ triggers heat stress alert with night-time hydration guidance.
6. **Frost Hazard**: Ambient temperature $< 2.0^\circ\text{C}$ triggers frost alert warning against overhead sprinkling.
7. **Sensor Health & Drift**: Flags stuck sensors, offline transmitters, and telemetry outliers.
8. **Invalid / Stale Telemetry**: Disqualifies `INVALID` readings from driving recommendations and emits `NO_RECENT_TELEMETRY`.

---

## 5. Agronomic Intelligence Logic
- **Telemetry Inputs**: Consumes latest canonical `SensorReading` points from `SensorReadingRepository`.
- **Field Context**: Retrieves parcel crop type (`COTTON`, `WHEAT`, `MELON`, etc.), soil texture (`LOAM`, `SANDY`, `CLAY`), and cultivated area in hectares.
- **Water Budget Integration**: Queries `WaterBudget` via `WaterBudgetDataPort`. If remaining available quota $\le 0\text{ m}^3$, flags `waterBudgetConstrained = true` and attaches `WATER_BUDGET_EXHAUSTED` constraint.
- **Hydraulic Interlock Integration**: Queries `HydraulicSafetyInterlock`. If pipe burst or catastrophic pressure drop is active, generates `IRRIGATION_BLOCKED` advisory with $0\text{ m}^3$ recommended volume.
- **Recommendation Synthesis**: When safe and quota-supported, computes recommended volume ($\approx 350\text{ m}^3/\text{ha}$ for deficit mitigation) and schedules nocturnal watering windows to prevent transpirational loss.

---

## 6. AI Architecture & Advisory Boundaries
- **Deterministic Authoritative Layer**: All calculations (soil moisture deficit, water volume, flow rates, duration, safety blocks, quota constraints) are performed deterministically in pure Java domain services.
- **AI Advisory Role**: The `IntelligenceAIExplanationPort` generates non-authoritative narrative summaries, agronomical insights, and trade-off commentary.
- **Zero Hallucination / Hard Boundaries**:
  - AI **cannot** override hydraulic safety interlocks.
  - AI **cannot** exceed water quotas.
  - AI **cannot** approve or dispatch irrigation plans.
  - AI **cannot** actuate physical valves or pumps.
- **Fallback Guarantee**: In case of Gemini SDK timeout, rate limit, or network disconnection, the platform falls back seamlessly to deterministic agronomic narratives without failing any user requests.

---

## 7. Database Strategy
- **Liquibase ChangeSet**: `changes/013-create-intelligence-domain-tables.sql` added to `db.changelog-master.xml`.
- **Tables**:
  - `agronomic_assessments`: Stores parcel evaluation aggregates with primary signal, severity, trend, freshness, confidence, hydraulic lock flag, and water budget flag.
  - `agronomic_assessment_signals`: Stores supporting signal items linked via foreign key with `ON DELETE CASCADE`.
- **Indexes**:
  - `idx_agronomic_assessments_field_assessed` on `(field_id, assessed_at DESC)`
  - `idx_agronomic_assessments_field_signal` on `(field_id, primary_signal)`
  - `idx_assessment_signals_assessment_id` on `(assessment_id)`

---

## 8. REST API Specifications
All endpoints enforce JWT authentication and tenant ownership:

| HTTP Method | Path | Summary | Role Authorization |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/fields/{fieldId}/intelligence/evaluate` | Evaluates telemetry, persists assessment, and outputs recommendations | Farmer (Own), Operator, Admin |
| `GET` | `/api/v1/fields/{fieldId}/intelligence/assessment` | Retrieves latest agronomic assessment | Farmer (Own), Operator, Admin |
| `GET` | `/api/v1/fields/{fieldId}/intelligence/signals` | Retrieves normalized signals with freshness status | Farmer (Own), Operator, Admin |
| `GET` | `/api/v1/fields/{fieldId}/intelligence/history` | Paginated assessment history | Farmer (Own), Operator, Admin |
| `POST` | `/api/v1/fields/{fieldId}/recommendations/evaluate` | Generates agronomic recommendation proposal | Farmer (Own), Operator, Admin |
| `GET` | `/api/v1/fields/{fieldId}/anomalies` | Lists parcel anomalies | Farmer (Own), Operator, Admin |
| `GET` | `/api/v1/anomalies/{id}` | Gets anomaly details | Farmer (Own), Operator, Admin |
| `PATCH` | `/api/v1/anomalies/{id}/acknowledge` | Acknowledges anomaly | Farmer (Own), Operator, Admin |
| `PATCH` | `/api/v1/anomalies/{id}/resolve` | Resolves anomaly | Farmer (Own), Operator, Admin |

---

## 9. Frontend Architecture & User Experience
- **FSD Structure**: Implemented under `frontend/src/entities/intelligence/` and `frontend/src/features/intelligence/`.
- **Components**:
  - `TelemetryFreshnessBadge`: Visual badge for `FRESH` (emerald), `STALE` (amber), and `MISSING` (rose).
  - `MoistureTrendIndicator`: Visual trend direction ($\nearrow, \searrow, \to$) with rate of change per hour.
  - `AgronomicAssessmentCard`: Interactive card showing primary signal, confidence rating, freshness badge, trend, safety interlock banner, water quota warnings, supporting signals, and a "Re-evaluate" action.
  - `IntelligencePipelineFlow`: Step-by-step decision-support roadmap (Telemetry $\to$ Assessment $\to$ Safety Interlock $\to$ Recommendation $\to$ Human Review $\to$ Smart Plan).
- **Page Integration**:
  - `RecommendationsPage.tsx`: Integrated `IntelligencePipelineFlow` and `AgronomicAssessmentCard`.
- **Internationalization (i18n)**: Fully localized across **Uzbek (`uz`)**, **Russian (`ru`)**, and **English (`en`)**.

---

## 10. Security & Anti-IDOR Enforcement
- **Farmer Isolation**: A farmer querying another farmer's field (`GET /api/v1/fields/{foreignId}/intelligence/*`) receives `HTTP 404 Not Found` (anti-enumeration), preventing discovery of parcel IDs or crop data.
- **Operator District Scope**: Operators have authorized read-only monitoring access across Yangiariq district.
- **Admin Full Access**: Administrators have system-wide authorized access.
- **Verified by Tests**: Validated in `IntelligenceSecurityIntegrationTest.java`.

---

## 11. Test Verification Results
All tests run in clean environments with 0 failures:

```text
Backend Tests:       466 passed (0 failures, 0 errors, 0 skipped)
Frontend Tests:      126 passed (0 failures across 16 test files)
Architecture Tests:   51 ArchUnit rules passed (100% compliant)
Integration Tests:    Passed (Security, Anti-IDOR, Persistence, API)
TypeScript (tsc -b):  0 errors
ESLint (eslint .):    0 errors / 0 warnings
Production Build:     PASS (Vite production bundle built successfully)
```

---

## 12. Documentation Index
- `docs/telemetry-intelligence.md` (Created): Complete architecture, quality rules, freshness policies, trend math, and safety hierarchy.
- `docs/agronomic-recommendations.md` (Created): Recommendation model, evidence structure, confidence calibration, and plan distinction.
- `docs/milestone-08.3-plan.md` (Created): Architectural plan and execution blueprint.
- `docs/anomaly-detection.md` (Updated): Anomaly detection rules, thresholds, and invalid/stale telemetry handling.
- `docs/module-boundaries.md` (Updated): Added `intelligence` bounded context contracts and ports.
- `ARCHITECTURE.md` (Updated): Documented Milestone 08.3 architecture and references.
- `README.md` (Updated): Documented `intelligence` bounded context and documentation links.

---

## 13. Known Limitations & Out of Scope
1. **No Autonomous Actuation**: The platform does not directly operate valves, pump switches, or mechanical gates. All recommendations must be reviewed and approved by human operators or farmers.
2. **No Hardware Protocols (MQTT/Modbus/Zigbee)**: The platform uses domain abstractions (`SensorProvider` / `SensorReadingRepository`); raw hardware edge gateway protocols will be introduced in future integration milestones.
3. **No Machine Learning Neural Networks**: Classification and anomaly detection remain deterministic, rule-based, and explainable to eliminate uncalibrated probabilistic hallucinations.

---

## 14. Recommended Next Milestone
Based on the verified architecture after Milestone 08.3:
**Recommended Next Milestone: Milestone 09.0 — Operational Field Dispatch & Work Order Execution**
- Introduces mobile-friendly field work orders generated from approved `SmartIrrigationPlan` records.
- Enables pump operators and farmers to log valve openings, canal gate adjustments, and actual start/end delivery times.
- Compares planned vs. actual operational delivery variance to close the telemetry feedback loop.
