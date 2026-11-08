# YANGIARIQ AQUA AI - ARCHITECTURE BLUEPRINT

## 1. Executive Summary

**Yangiariq Aqua AI** is an agricultural decision-support platform designed for Yangiariq district (Khorezm Region, Uzbekistan). The primary mission of the platform is to optimize water allocation, prevent drought stress across agricultural fields, detect and mitigate distribution canal incidents, and empower farmers and district water management operators with timely, AI-driven advisory and telemetry visualization.

This document outlines the architectural foundations, architectural decisions (ADRs), module boundaries, domain rules, and clean engineering standards governing the platform.

---

## 2. Architectural Paradigm: Modular Monolith

To maximize development velocity during the MVP and early-growth phases while eliminating premature distributed system complexity, Yangiariq Aqua AI is built as a **Modular Monolith**.

```
+----------------------------------------------------------------------------------+
|                             SPRING BOOT APPLICATION                              |
|                                                                                  |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|  |     auth     |  |     user     |  |    field     |  |         water         | |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|                                                                                  |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|  |   problem    |  |  dashboard   |  |     risk     |  | notification          | |
|  +--------------+  +--------------+  +--------------+  +-----------------------+ |
|                                                                                  |
|  +-----------------------------------------------------------------------------+ |
|  |                     network (Regional Conveyance & Dispatch)                | |
|  |            [CanalDispatchOptimizer / Resilience / Topology / Leaflet GIS]    | |
|  +-----------------------------------------------------------------------------+ |
|                                                                                  |
|  +--------------------------------+  +-----------------------------------------+ |
|  |         ai (Abstraction)       |  |          sensor (Abstraction)           | |
|  |   [AiProvider -> Gemini/Mock]  |  | [SensorProvider -> Simulated/Future IoT]| |
|  +--------------------------------+  +-----------------------------------------+ |
|                                                                                  |
|  +-----------------------------------------------------------------------------+ |
|  |                         anomaly (Intelligent Alerting)                       | |
|  |           [AnomalyDetector SPI -> Rules / Z-Score / Health / Bursts]         | |
|  +-----------------------------------------------------------------------------+ |
+----------------------------------------------------------------------------------+
                                        |
                                        v
                       +---------------------------------+
                       |     POSTGRESQL (via Liquibase)  |
                       +---------------------------------+
```

### Key Principles
1. **Single Deployable Unit, Strict Internal Decoupling**: The system runs as a single process in development and production, but package boundaries are treated as strict module contracts.
2. **Path to Microservices**: If an individual capability (such as AI analytics or high-throughput IoT ingestion) requires independent scaling in the future, its module can be extracted into an independent microservice with zero changes to other domain models.
3. **No Monolithic Service Bloat**: Business logic is encapsulated in cohesive application services dedicated to specific aggregate roots.

---

## 3. Backend Architecture: Clean Package-by-Feature

Every business module follows the Clean / Hexagonal Architecture structure:

```
com.aquaai.<module>
├── api
│   ├── <Module>Controller.java          # REST entrypoints (HTTP status codes, routing)
│   └── dto                              # Strictly segregated Request & Response DTOs
│       ├── Create<Entity>Request.java
│       └── <Entity>Response.java
├── application
│   └── <Module>Service.java             # Use case orchestration, transactions, domain events
├── domain
│   ├── <Entity>.java                    # JPA Domain entities with business invariants
│   ├── <Entity>Repository.java          # Spring Data JPA interface (Ports)
│   └── valueobjects / enums             # Domain types, status enums, value objects
└── infrastructure
    ├── <Module>RepositoryImpl.java      # Custom repository implementations (Adapters)
    └── external                         # Third-party integrations & SDK adapters
```

### Boundary Rules
- **No Controller Business Logic**: Controllers only validate inputs, call the application service, and wrap results into `ApiResponse<T>`.
- **No Direct Entity Exposure**: REST controllers never return JPA entities. They only accept and return DTOs.
- **No Direct Repository Exposure to Controllers**: Controllers never inject or call Repositories directly. This rule is enforced automatically via ArchUnit tests (`ArchitectureTests.java`).
- **Encapsulated Dependencies**: Cross-module communication occurs strictly via public Application Service interfaces or Domain Events, never through direct database joins across modules.

---

## 4. Artificial Intelligence (AI) Abstraction

The platform delivers smart water optimization using the Gemini API, but the domain layer **never depends directly on the Google Gemini SDK**.

```
              +---------------------------+
              |   AiService (Application) |
              +---------------------------+
                            |
                            v
              +---------------------------+
              |   <<interface>>           |
              |   AiProvider (Domain)     |
              +---------------------------+
                     /                 \
                    /                   \
+-----------------------+     +-----------------------+
|  GeminiAiProvider     |     |   MockAiProvider      |
|  (Infrastructure)     |     |   (Infrastructure)    |
+-----------------------+     +-----------------------+
```

### Design Rationale
- **Pluggability**: The provider can be swapped between Gemini, OpenAI, Claude, Local LLMs, or an offline Mock AI without altering a single line in `AiService` or the domain.
- **Testability & Determinism**: Unit and integration tests run against `MockAiProvider`, eliminating external network dependencies, rate limits, and API cost overhead.
- **Provider Switching**: Controlled by the `app.ai.provider` configuration property (`gemini` or `mock`).

---

## 5. IoT Sensor Abstraction & Simulation Strategy

Currently, physical IoT sensors do not exist in the fields of Yangiariq. However, the system architecture treats sensor telemetry as first-class citizens.

```
              +--------------------------------+
              |  SensorService (Application)   |
              +--------------------------------+
                               |
                               v
              +--------------------------------+
              |         <<interface>>          |
              |     SensorProvider (Domain)    |
              +--------------------------------+
                               |
              +--------------------------------+
              |     SimulatedSensorProvider    |
              |        (Infrastructure)        |
              +--------------------------------+
            (Future: MqttSensorProvider / HttpSensorProvider)
```

### Guarantees
- The domain models (`SensorReading`, `SensorType`) model real-world telemetry (soil moisture %, water flow rate, canal level, battery levels).
- `SimulatedSensorProvider` emits realistic telemetry bounded to Yangiariq's regional climate and crop water intake profiles.
- When physical hardware (LoRaWAN / MQTT gateways) is installed in later phases, an `MqttSensorProvider` adapter can be added with zero changes to the core business logic.

---

## 5.5. Telemetry Anomaly Detection & Alerting Architecture

Milestone 08.0 adds the **Telemetry Anomaly Detection & Intelligent Alerting System** (`com.aquaai.anomaly`).

### Guarantees
- **Decoupled Ingestion**: The `sensor` module publishes domain events (`TelemetryReadingRecordedEvent`). Anomaly detection consumes these asynchronously, ensuring zero coupling.
- **Pluggable Detector SPI**: Contract `AnomalyDetector` allows independent registration of deterministic rules (soil moisture deficit, flow spikes, pressure drop), statistical algorithms (Z-score deviation against 24h moving baselines), hardware health monitoring (stuck sensor, possible drift, offline heartbeats), and multi-metric corroboration (suspected pipe burst requiring both high flow and pressure drop).
- **Active Alert Deduplication & State Machine**: State transitions (`DETECTED` -> `ACKNOWLEDGED` -> `INVESTIGATING` -> `RESOLVED` / `DISMISSED`) enforce terminal state immutability, mandatory resolution notes, optimistic concurrency locking, and alert cooldown throttling (30 min).
- **Simulation Isolation**: Anomalies detected on simulated telemetry are tagged `isSimulated=true` and never silently trigger production alerts to farmers.

---

## 6. Database & Migration Strategy

- **DBMS**: PostgreSQL 16
- **Migration Engine**: Liquibase (`db.changelog-master.xml`)
- **Primary Keys**: UUIDs (`gen_random_uuid()`) for distributed scalability and secure identifiers.
- **Audit Columns**: Every table inherits `created_at`, `updated_at`, and `version` (optimistic locking) through `BaseAuditableEntity`.
- **Integrity**: Referential integrity enforced with explicit foreign key cascades (`ON DELETE CASCADE` or `ON DELETE SET NULL`) and compound indexes.
- **Rule**: Manual changes to the production schema are strictly forbidden. All schema modifications must occur via versioned Liquibase changelogs.

---

## 7. Security Architecture

- **Mechanism**: Hybrid JWT & Stateful Session Security:
  - **Access Tokens**: Stateless signed JWT (HS256) for zero-database verification per request.
  - **Refresh Tokens**: Cryptographically secure UUIDs rotated on every refresh. Only SHA-256 digests (`token_hash`) are stored in `refresh_token_sessions`. Supports multi-device tracking.
  - **Reuse Detection**: Presenting an already-revoked refresh token triggers an immediate automated compromise mitigation that invalidates all active sessions for that user across all devices.
  - **Password Reset**: Single-use tokens stored as SHA-256 digests with expiration and non-enumerating responses. Dispatched via decoupled `PasswordResetNotificationProvider`.
- **Roles & Permissions**:
  - `FARMER`: Manages their own fields, logs irrigation, submits problem reports, views field recommendations.
  - `OPERATOR`: Manages district-wide water scheduling, monitors incidents, views aggregated analytics.
  - `ADMIN`: User management, global configuration, system auditing.
- **Ownership-Aware Authorization**: `SecurityContextHelper` provides programmatic evaluation of resource ownership against the authenticated JWT principal, allowing role-based overrides (e.g. `OPERATOR` or `ADMIN`) where needed.
- **Data Protection**: Passwords hashed using BCrypt. Secrets injected exclusively via environment variables (`.env`). Secrets and tokens are filtered and never logged.

---

## 8. Frontend Architecture

The frontend is structured according to **Feature-Oriented Architecture**:

```
frontend/src/
├── app/          # Global application setup, TanStack Query provider, global styles
├── pages/        # Route components (Dashboard, Fields, Irrigation, Incidents, Settings)
├── widgets/      # Compositional blocks (Sidebar, NavigationHeader, MetricCards)
├── features/     # Feature interactions (field-map, water-logging, problem-reporting)
├── entities/     # Domain models & UI cards (user, field, sensor, irrigation)
└── shared/       # Reusable primitives (shadcn/ui, API client, i18n, formatting)
```

### State Management
- **Server State**: Managed exclusively by **TanStack Query** (caching, background revalidation, optimistic updates).
- **Client State**: Minimal local state (React hooks / Zustand where needed for non-server UI state).
- **Forms**: React Hook Form validated with **Zod** schemas.
- **Localization**: Native dictionary structure supporting Uzbek (`uz`), Russian (`ru`), and English (`en`).

---

## 9. Water Management & Irrigation Domain

Following the user/auth and field management milestones, the platform establishes the **Water Management & Irrigation (`com.aquaai.water`)** domain context:

```text
Field
  │
  ├── WaterAllocation (Linked to WaterSource: Canal, Reservoir, Well)
  │
  └── IrrigationPlan (Scheduled water volume & method)
           │
           └── IrrigationEvent (Real executed delivery & actual consumption m³)
```

### Planned vs Actual Accounting & Future Capabilities
- **Domain & Database Foundation (Milestone 06.1)**: Robust pure domain models, invariants, status lifecycles, and PostgreSQL persistence capturing allocated quotas (`WaterAllocation`), planned irrigation schedules (`IrrigationPlan`), and recorded operational deliveries (`IrrigationEvent`).
- **Application Services & REST API (Milestone 06.2)**: Production-ready clean architecture REST APIs (`/api/v1/water-sources`, `/api/v1/water-allocations`, `/api/v1/irrigation-plans`, `/api/v1/irrigation-events`), IDOR protection with anti-enumeration (404), cross-aggregate invariant enforcement (422), pagination, whitelist sorting, and OpenAPI 3 contracts.
- **Water Rules, Consumption & Efficiency (Milestone 06.3)**: Real-time volumetric consumption accounting, non-negative remaining quota clamping, over-consumption detection and excess volume tracking, planned vs actual variance ratios, parcel periodic water summaries, and zero N+1 database aggregations.
- **Water Frontend UI: Consumption, Quotas & Variance Dashboards (Milestone 06.4)**: Production-ready FSD Water operations dashboard (`frontend/src/entities/water`, `frontend/src/features/water`, `frontend/src/pages/irrigation/IrrigationPage.tsx`), integrating TanStack Query, Zod form validations, clamped progress bars, over-consumption alert banners, planned vs actual variance visualizations, field periodic summaries, regional water source catalog with RBAC, and full i18n (`uz`, `ru`, `en`).
- **Water + GIS Intelligence Integration (Milestone 06.5)**: Unified spatial cartography combining field geometry (`[lon, lat]` RFC 7946 GeoJSON), regional water sources (`CANAL`, `WELL`, `RESERVOIR`), volumetric allocations, actual metered consumption, dynamic supply linkage geodesics, and real-time over-quota warnings. Implements single-roundtrip batch backend aggregations (`/api/v1/gis/overview`) without PostGIS, strict tenant anti-IDOR isolation, interactive parcel side panel, multi-filter toolbar, layer toggles, and resilient non-blocking geometry handling.
- **Sensor Domain & Telemetry Foundation (Milestone 07.1)**: Pure Java domain model (`Sensor` aggregate root, `SensorReading` value objects), physical parameter invariants, device lifecycle state machine (`ACTIVE`, `INACTIVE`, `MAINTENANCE`, `OFFLINE`), `SensorProvider` SPI with realistic Yangiariq physical drift simulation (`SimulatedSensorProvider`), Liquibase migration `008-create-sensor-domain-tables.sql`, anti-IDOR security via parcel tenancy, and REST APIs under `/api/v1/sensors` and `/api/v1/fields/{fieldId}/sensors`.
- **Time-Series Storage & Telemetry Analytics (Milestone 07.3)**: High-performance historical telemetry analytics and bucketing engine across 5 temporal granularities (`5m`, `15m`, `1h`, `1d`, `1w`). Evaluated TimescaleDB extension compatibility against deployment environment (`postgres:16-alpine`), delivering a high-performance PostgreSQL-native architecture with composite B-tree indexes, lightweight Spring Data projections (`SensorReadingPointProjection`), and single-pass linear chronological bucketing. Enforces modality-specific mathematical aggregation policies (`AggregationPolicy`: AVG/MIN/MAX for soil moisture, flow, temperature, salinity; SUM/MIN/MAX for rainfall), rolling 24h parcel summaries, hardware fleet availability tracking (`communicating` vs `unresponsive` based on `lastSeenAt`), data quality metrics (`VALID`, `SUSPECT`, `INVALID`), retention policy abstraction (`TelemetryRetentionService`), interactive frontend telemetry dashboard with Recharts, and GIS side-panel telemetry integration.
*(Note: Milestone 07.2 — High-Throughput Telemetry Ingestion Pipeline was identified as not present in the repository prior to 07.3; its message streaming components remain scheduled for future streaming milestones).*
- **Telemetry Anomaly Detection & Intelligent Alerting System (Milestone 08.0)**: Statistical Z-score and configurable agricultural threshold anomaly detection (`com.aquaai.anomaly`), multi-metric pipe burst corroboration (flow surge + pressure drop), alert lifecycle state machine (`DETECTED`, `ACKNOWLEDGED`, `INVESTIGATING`, `RESOLVED`, `DISMISSED`), active deduplication, simulation isolation, full audit trails, and GIS side-panel anomaly integration.
- **AI Agronomic Recommendations & Decision Support System (Milestone 08.1)**: Explainable decision support engine (`com.aquaai.recommendation`) synthesizing telemetry, anomalies, weather forecasts, and historical water allocations. Pure Java domain model, deterministic agronomic safety rules, hydraulic safety interlock (immediate `INSPECT_PIPE_BURST` with `CRITICAL` priority suppressing irrigation), Google Gemini 1.5 Flash advisory adapter with structured JSON schema, zero PII exposure, graceful fallback to mock advisor, Hargreaves-Samani ET0 estimation, multi-factor confidence calibration, 4-hour cooldown deduplication, anti-IDOR security with 404 anti-enumeration, trilingual frontend dashboard (`RecommendationsPage`), and GIS side-panel advisory integration.
- **Smart Irrigation Planning & Water Allocation Optimization (Milestone 08.2)**: Safe, explainable, and operational irrigation decision-support engine (`com.aquaai.irrigation`). Pure Java domain model (`SmartIrrigationPlan` aggregate root, value objects for `WaterBudget`, `IrrigationRequirement`, `FlowRate`, `IrrigationWindow`), 8-stage lifecycle state machine (`DRAFT` -> `PROPOSED` -> `APPROVED` -> `SCHEDULED` -> `IN_PROGRESS` -> `COMPLETED`, plus `CANCELLED` and `EXPIRED`), hard constraint precedence hierarchy (`SAFETY` -> `WATER_QUOTA` -> `INFRASTRUCTURE` -> `AGRONOMIC` -> `WEATHER` -> `FIELD_PRIORITY` -> `AI_EXPLANATION`), hydraulic safety interlock (blocking plans on suspected pipe bursts or pressure drops), root-zone soil water deficit model accounting for crop coefficient ($K_c$), Hargreaves-Samani reference $ET_0$, forecast rainfall offsets, and irrigation method application efficiencies ($\eta$), multi-parcel priority scoring engine (0-100), conflict resolution for water quotas and nocturnal scheduling, idempotent generation, anti-IDOR parcel isolation with 404 anti-enumeration, full Liquibase migrations (`012-create-smart-irrigation-planning-tables.sql`), FSD frontend architecture with water budget KPI cards, interactive schedule timeline, field priority table, approval dialog with warning acknowledgement checkbox, cancel dialog with mandatory reason, and GIS side-panel integration. Strictly planning only: no autonomous actuator or pump control.
- **Telemetry Intelligence, Anomaly Detection & Agronomic Recommendations (Milestone 08.3)**: Unified decision-support intelligence layer (`com.aquaai.intelligence`) converting canonical physical sensor telemetry into normalized `TelemetrySignal`s, temporal trend indicators ($\Delta\text{moisture}/\Delta\text{time}$), and SLA-based freshness evaluations (`FRESH`, `STALE`, `MISSING`). Features a deterministic agronomic assessment engine producing `AgronomicAssessment` aggregates (`SOIL_TOO_DRY`, `SOIL_TOO_WET`, `HIGH_TEMPERATURE`, `LOW_TEMPERATURE`, `HIGH_HUMIDITY`, `LOW_HUMIDITY`, `ABNORMAL_FLOW`, `PRESSURE_DROP`, `NO_RECENT_TELEMETRY`), integrated with the authoritative `HydraulicSafetyInterlock` and volumetric `WaterBudget`. Strictly decision-support only with zero autonomous hardware actuation. Fully verified with 466 backend tests, 51 ArchUnit architecture rules, 126 frontend tests, and complete anti-IDOR security.
- **Unified Farm Intelligence Dashboard & Action Center (Milestone 08.4)**: Authoritative read model and decision-support orchestrator (`com.aquaai.dashboard`). Eliminates fragmented multi-screen navigation by providing a unified operational workspace answering "What needs attention, why, is there water, is irrigation safe, and what action should be taken" within 10 seconds of login. Features single-pass indexed batch aggregation (`GET /api/v1/dashboard/overview`) with zero N+1 queries, deterministic parcel priority scoring ($0\dots 100$) respecting soil moisture stress thresholds and `HydraulicSafetyInterlock`, centralized Action Center orchestrating urgent anomalies, recommendations, and plan executions with one-click navigation CTAs, volumetric water budget breakdown with non-negative arithmetic, 6-stage irrigation lifecycle pipeline tracking, interactive GIS map synchronization, responsive desktop table and mobile cards, and trilingual i18n (`uz`, `ru`, `en`). Strict tenant anti-IDOR isolation (Farmer own parcels with 404 anti-enumeration, Operator district scope, Water Admin global scope). Fully verified with 483 backend tests, 55 ArchUnit architecture rules, and 141 frontend tests.
- **Decision-to-Action Workflow & Irrigation Operations Center (Milestone 08.5)**: Continuous, safe, fast operational workspace orchestrating domain capabilities (`com.aquaai.operations`). Eliminates fragmented multi-screen journeys by providing a single contextual workflow moving from "Problem Detected" to "Field Action Completed & Verified" with minimal cognitive friction. Non-duplicative orchestration layer: existing aggregates (`SmartIrrigationPlan`, `WaterBudget`, `HydraulicSafetyInterlock`, `AgronomicRecommendation`, `IrrigationEvent`) remain authoritative. Deterministically derives 16 operational workflow states and a 7-step chronological timeline (`DETECTED` -> `RECOMMENDATION_READY` -> `PLAN_PROPOSED` -> `APPROVAL_REQUIRED` -> `SCHEDULED` -> `IN_PROGRESS` -> `VERIFIED`). Revalidates safety interlocks (pipe burst/pressure drop block), seasonal volumetric quotas, and channel dispatch conflicts upon human approval. Enforces mandatory operational constraints acknowledgement checkbox (`acknowledgeConstraints = true`) and mandatory cancellation reasons. Strictly decision-support only: zero autonomous physical actuator/valve control, clear manual confirmation terminology. Features post-irrigation closed-loop verification comparing planned vs actual volume and duration, calculating root-zone soil moisture change ($\Delta\%$), and generating learning loop calibration evidence. Anti-IDOR security (Farmer own parcels with 404 anti-enumeration, Operator district scope, Admin global scope). Fully verified with 500 backend tests, 59 ArchUnit architecture rules, and 153 frontend tests.
- **Farm Performance, Historical Intelligence & Analytics (Milestone 08.6)**: Read-model and historical performance intelligence workspace (`com.aquaai.analytics`). Closes the operational loop by answering *"What happened before?", "What changed?", "How well did we perform?", "What patterns can we see?", "What should we improve?"*. Transforms operational time-series data into actionable decisions with zero synthetic/fake AI metrics. Exposes batch aggregations with zero N+1 queries across multi-parcel scopes (`GET /api/v1/analytics/overview`, `/water`, `/irrigation`, `/telemetry`, `/verification`, `/trends`, `/fields`, `/insights`). Implements rigorous metric governance distinguishing water quota utilization from plan variance, transparent data confidence levels (`COMPLETE`, `PARTIAL`, `INSUFFICIENT`), explainable composite performance index ($0\dots 100$) with absolute safety dominance, period-over-period delta comparisons with safe fallback when historical data is absent ("Insufficient historical data"), and deterministic rule-based insight engine emitting WHAT, WHY, EVIDENCE, and NEXT STEP deep-linking into existing operational workflows. Guarded by 65 ArchUnit architecture tests, 537 backend tests, and 163 frontend tests.
- **Predictive Farm Intelligence & Forecasting (Milestone 08.7)**: Forward-looking decision support capability (`com.aquaai.prediction`). Transforms historical analytics into predictive intelligence answering *"What is likely to happen next?"* across 3 standardized horizons (`NEXT_24_HOURS`, `NEXT_48_HOURS`, `NEXT_7_DAYS`). Strictly advisory: zero autonomous valve, pump, or allocation execution. Evaluates 6 core prediction capabilities: soil moisture trajectories via FAO-56 bounded regression, irrigation need likelihood via crop-specific MAD thresholds, volumetric water demand aggregation, water quota headroom risk, irrigation plan hydraulic safety risk (strictly enforcing `BLOCKED` status on active hydraulic anomalies), and 14-day telemetry reliability forecasting. Features deterministic provider abstraction (`PredictionProvider` SPI), immutable historical prediction persistence (`predictions` table via Liquibase migration 015), anti-IDOR isolation with 404 anti-enumeration, explainable AI modal (WHAT, WHY, EVIDENCE, ASSUMPTIONS, NEXT STEP), dashboard summary card integration, GIS forecast navigation, empirical calibration tracking, and dedicated responsive workspace at `/predictions`. Guarded by 72 ArchUnit rules, 565 backend tests, and 174 frontend tests.
- **Predictive Model Evaluation, Accuracy & Continuous Intelligence (Milestone 08.8)**: Complete empirical verification and continuous intelligence feedback loop (`com.aquaai.prediction`). Closes the learning loop by answering *"How accurate was that prediction?", "Where did it succeed/fail?", "Why did it fail?", "Is the model improving or drifting over time?"*. Implements domain services `PredictionOutcomeMatcher` (authoritative temporal windows: $\pm 3\,\text{h}$ sensor observations, qualifying irrigation events, actual water consumption records), `PredictionEvaluationAggregator` (arbitrary-precision MAE, RMSE, safe MAPE avoiding zero division, Mean Bias Error (MBE), confusion matrix with $TP, TN, FP, FN, INT$, Precision, Recall, and F1 score), and `EvaluationInsightGenerator` (deterministic actionable recommendations for bias correction, telemetry repair, and calibration re-weighting). Operational governance: hydraulic safety interlocks halting planned irrigations are strictly classified as `OUTCOME_INTERRUPTED` rather than false positives, honoring physical system integrity. Absolute honesty: minimum sample size gating ($n \ge 10$) suppresses misleading percentages and displays explicit 'Insufficient data for evaluation' notifications. Automated performance drift detection flags $\ge 30\%$ error increases over recent 7-day vs 30-day historical baselines. Human-in-the-loop audit records alignment across AI recommendation, human decision, and realized operational outcome. Guarded by 72 ArchUnit rules, 599 backend tests, and 186 frontend tests.
- **Adaptive Intelligence, Model Governance & Controlled Calibration (Milestone 08.9)**: Complete predictive governance, versioning, anti-leakage backtesting, and controlled rollout lifecycle (`com.aquaai.prediction`). Advances the platform from observing degradation to controlled, explainable adaptation: $\text{Insight} \to \text{Proposal} \to \text{Backtest} \to \text{Human Review} \to \text{Version} \to \text{Controlled Rollout} \to \text{Monitoring} \to \text{Rollback}$. Strict zero autonomous self-modification principle: the system NEVER changes parameters or logic automatically without explicit human administrator approval (`ROLE_ADMIN`). Hard hydraulic safety constraints (`pipeBurstPressureBar`, `emergencyPressureThreshold`, `hydraulicSafetyInterlock`, etc.) are permanently immutable and blocked from calibration at domain, use-case, and API boundaries. Features immutable method versioning (`PredictionMethodVersion`), candidate proposal workflow (`CalibrationProposal`), deterministic anti-leakage historical backtesting (`BacktestingEngine`), multi-factor regression guard (`RegressionGuard`), controlled rollout staging (`SHADOW`, `CANARY`, `ACTIVE`), instant 1-click emergency rollback (`RollbackManager`), and comprehensive auditability (`GovernanceAuditLog`). Guarded by 74 ArchUnit rules, 630 backend tests, and 196 frontend tests.
- **Regional Water Accounting, Distribution Equity & Conservation Intelligence (Milestone 08.15)**: Authoritative regional water accounting layer (`com.aquaai.wateraccounting`) connecting planned distribution, actual canal delivery, field consumption, conveyance losses, allocation compliance, and inter-cluster fairness:
  - Universal mass-conservation water balance ($Closing = Opening + Inflow - Consumed - Losses - ReturnFlow$) with automated tolerance discrepancy detection.
  - Traceable accounting chain preventing double-counting between source abstraction, canal transfer, parcel turnout delivery, and crop root-zone consumption.
  - Continuous conveyance loss accounting on canal reaches with upstream-downstream differential analysis and physical classification (`ACCEPTABLE_CONVEYANCE`, `VERIFIED_LOSS`, `SUSPECTED_LOSS`, `UNACCOUNTED_WITHDRAWAL`, `MEASUREMENT_DISCREPANCY`).
  - Parcel-level quota allocation compliance tracking with delivery shortfall and over-delivery alerts.
  - Statistical distribution equity analysis ($m^3/\text{ha}$, sample variance, coefficient of variation $CV$, and rotational delivery delay hours) across Water User Associations (WUAs) to eliminate tail-end disadvantages.
  - Multi-stage boundary reconciliation with human operator review workflow and immutable audit log notes.
  - Evidence-based water conservation intelligence providing verifiable recommendations and engineering recovery calculations (guaranteeing zero fabricated savings).
  - Modern responsive frontend with mass-balance visualization, searchable transaction ledger, equity comparisons, and field accounting drill-down. Guarded by ArchUnit rules 89–94, 755 backend tests, and 247 frontend tests.
- **Regional Water Accountability, Incident Investigation & Corrective Action Governance (Milestone 08.16)**: Authoritative operational accountability and incident governance layer (`com.aquaai.accountability`) transforming findings into auditable investigations, assigned responsibilities, documented corrective actions, and independently verified resolutions:
  - 10 deterministic incident categories (`WATER_BALANCE_DISCREPANCY`, `CONVEYANCE_LOSS`, `UNACCOUNTED_WITHDRAWAL`, `ALLOCATION_EXCEEDED`, `UNDER_DELIVERY`, `DISTRIBUTION_DISPARITY`, `HYDRAULIC_SAFETY`, `SENSOR_DATA_QUALITY`, `OPERATIONAL_NON_COMPLIANCE`, `OTHER`).
  - Deterministic severity model with strict wall-clock SLA review horizons (`CRITICAL`: 4h, `HIGH`: 24h, `MEDIUM`: 72h, `LOW`: 168h, `INFORMATIONAL`: none).
  - Evidence confidence taxonomy (`VERIFIED`, `PARTIALLY_VERIFIED`, `UNVERIFIED`, `SIMULATED`, `INSUFFICIENT_DATA`) preserving data lineage and prohibiting false elevation of simulated data.
  - Presumption of innocence: non-accusatory governance treating discrepancies as operational findings rather than automatic misconduct.
  - 12-state deterministic incident lifecycle machine (`DETECTED` -> `UNDER_REVIEW` -> `OPEN` -> `INVESTIGATING` -> `ACTION_PROPOSED` -> `ACTION_APPROVED` -> `ACTION_IN_PROGRESS` -> `PENDING_VERIFICATION` -> `RESOLVED` -> `CLOSED`, plus `DISMISSED` and `REOPENED`).
  - Strict separation of duties (Four-Eyes Principle): action owners cannot verify resolutions unless documented administrative override is granted.
  - No auto-resolution: completing a corrective action strictly moves cases to `PENDING_VERIFICATION`.
  - Non-actuation constraint: zero direct physical gate manipulation or pump actuation (ArchUnit Rule 100).
  - Comprehensive responsive frontend with KPI metrics cards, searchable list table, 5-tab detail modal, and complete governance dialogs. Guarded by ArchUnit rules 95–100 (total 100 rules), 814 backend tests (798 passed), and 261 frontend tests (28 test files).
- **Regional Water Operations Performance, Outcome Measurement & Evidence-Based Impact Assessment (Milestone 08.17)**: Authoritative outcome measurement and impact assessment layer (`com.aquaai.performance`) connecting intelligence modules to evaluate physical reality:
  - Tripartite conceptual separation: strictly separates OUTPUT (activities/schedules) from OUTCOME (measured changes in physical state) from IMPACT (conservative, verified causal attribution).
  - Deterministic performance indicator catalog across delivery reliability, conveyance efficiency, allocation compliance, distribution equity ($CV$), irrigation efficiency, incident recurrence, data quality, and verified conservation savings.
  - Robust 5-point baseline comparability engine (`BaselineComparabilityEngine`) enforcing duration ratio (0.50–2.00), unit compatibility, geographic scope alignment, seasonal/climatic context matching, and $\ge 80\%$ completeness.
  - Conservative attribution hierarchy (`DIRECTLY_VERIFIED`, `STRONGLY_ASSOCIATED`, `ASSOCIATED`, `INCONCLUSIVE`, `NOT_ASSESSABLE`) with strict invariant: simulated or estimated baselines can NEVER yield directly verified attribution.
  - Non-actuation guarantee: purely observational, analytical, and governance layer with zero direct actuator control (ArchUnit Rule 106).
  - Comprehensive frontend workspace: overview KPI cards, dynamic trend charts with visual missing data breaks and simulated badges, indicator catalog, impact assessment registry with independent auditor review, and baseline/target management.
  - Multi-tenant anti-IDOR security preventing cross-farm data leakage. Guarded by 106 ArchUnit rules, 836 backend tests (820 passed, 0 failures, 16 skipped), and 275 frontend tests across 29 test files.
- **Future Integration (Upcoming Milestones)**:
  - High-throughput message streaming / MQTT broker integration.
  - Advanced hydraulic model calibration and canal head-gate automation.

---

## 10. Domain Documentation References

- **Regional Performance Architecture**: [`docs/regional-performance-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/regional-performance-architecture.md)
- **Performance Indicator Catalog**: [`docs/performance-indicator-catalog.md`](file:///home/izzatbek/Desktop/agronexsus/docs/performance-indicator-catalog.md)
- **Baseline and Comparability Methodology**: [`docs/baseline-and-comparability-methodology.md`](file:///home/izzatbek/Desktop/agronexsus/docs/baseline-and-comparability-methodology.md)
- **Outcome and Evidence-Based Impact Assessment**: [`docs/outcome-and-impact-assessment.md`](file:///home/izzatbek/Desktop/agronexsus/docs/outcome-and-impact-assessment.md)
- **Water Conservation Impact Verification**: [`docs/conservation-impact-verification.md`](file:///home/izzatbek/Desktop/agronexsus/docs/conservation-impact-verification.md)
- **Performance Data Quality and Reliability**: [`docs/performance-data-quality.md`](file:///home/izzatbek/Desktop/agronexsus/docs/performance-data-quality.md)
- **Regional Performance Reporting & Export**: [`docs/regional-performance-reporting.md`](file:///home/izzatbek/Desktop/agronexsus/docs/regional-performance-reporting.md)
- **Milestone 08.17 Completion Report**: [`docs/milestone-08.17-completion-report.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.17-completion-report.md)
- **Regional Water Accountability Architecture**: [`docs/regional-accountability-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/regional-accountability-architecture.md)
- **Incident Lifecycle and Governance**: [`docs/incident-lifecycle-and-governance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/incident-lifecycle-and-governance.md)
- **Investigation and Evidence Management**: [`docs/investigation-and-evidence-management.md`](file:///home/izzatbek/Desktop/agronexsus/docs/investigation-and-evidence-management.md)
- **Corrective Action Governance**: [`docs/corrective-action-governance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/corrective-action-governance.md)
- **Independent Verification and Separation of Duties**: [`docs/independent-verification.md`](file:///home/izzatbek/Desktop/agronexsus/docs/independent-verification.md)
- **Accountability SLA and Escalation Governance**: [`docs/accountability-sla-and-escalation.md`](file:///home/izzatbek/Desktop/agronexsus/docs/accountability-sla-and-escalation.md)
- **Milestone 08.16 Completion Report**: [`docs/milestone-08.16-completion-report.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.16-completion-report.md)
- **Regional Water Accounting & Conservation Intelligence**: [`docs/regional-water-accounting.md`](file:///home/izzatbek/Desktop/agronexsus/docs/regional-water-accounting.md)
- **Water Balance Methodology & Calculation Engine**: [`docs/water-balance-methodology.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-balance-methodology.md)
- **Conveyance Loss Accounting & Differential Analysis**: [`docs/conveyance-loss-accounting.md`](file:///home/izzatbek/Desktop/agronexsus/docs/conveyance-loss-accounting.md)
- **Water Distribution Equity & Inter-Cluster Fairness**: [`docs/water-distribution-equity.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-distribution-equity.md)
- **Water Accounting Reconciliation & Discrepancy Governance**: [`docs/water-accounting-reconciliation.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-accounting-reconciliation.md)
- **Evidence-Based Water Conservation Intelligence**: [`docs/water-conservation-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-conservation-intelligence.md)
- **Milestone 08.15 Completion Report**: [`docs/milestone-08.15-completion-report.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.15-completion-report.md)
- **Regional Water Network Architecture**: [`docs/water-network-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-network-architecture.md)
- **Network Topology & Capacity Engine**: [`docs/network-topology-and-capacity.md`](file:///home/izzatbek/Desktop/agronexsus/docs/network-topology-and-capacity.md)
- **Canal Dispatch Optimization & Fairness Policies**: [`docs/canal-dispatch-optimization.md`](file:///home/izzatbek/Desktop/agronexsus/docs/canal-dispatch-optimization.md)
- **Cooperative Irrigation Scheduling**: [`docs/cooperative-irrigation-scheduling.md`](file:///home/izzatbek/Desktop/agronexsus/docs/cooperative-irrigation-scheduling.md)
- **Network Resilience & What-If Stress Scenarios**: [`docs/network-resilience-scenarios.md`](file:///home/izzatbek/Desktop/agronexsus/docs/network-resilience-scenarios.md)
- **Hydraulic Safety Limitations & Zero Actuation**: [`docs/hydraulic-safety-limitations.md`](file:///home/izzatbek/Desktop/agronexsus/docs/hydraulic-safety-limitations.md)
- **Milestone 08.14 Completion Report**: [`docs/milestone-08.14-completion-report.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.14-completion-report.md)
- **Farm-Level Predictive Risk & Resilience Intelligence**: [`docs/farm-risk-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/farm-risk-intelligence.md)
- **Risk Assessment Rules & Prioritization Formulas**: [`docs/risk-assessment-rules.md`](file:///home/izzatbek/Desktop/agronexsus/docs/risk-assessment-rules.md)
- **Risk Confidence & Evidence Model**: [`docs/risk-confidence-and-evidence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/risk-confidence-and-evidence.md)
- **Milestone 08.13 Completion Report**: [`docs/milestone-08.13-completion-report.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.13-completion-report.md)
- **Context-Aware Adaptive Intelligence Engine**: [`docs/context-aware-prediction.md`](file:///home/izzatbek/Desktop/agronexsus/docs/context-aware-prediction.md)
- **Adaptive Method Selection & 9-Level Hierarchy**: [`docs/adaptive-selection.md`](file:///home/izzatbek/Desktop/agronexsus/docs/adaptive-selection.md)
- **Contextual Performance & Local Drift Detection**: [`docs/context-performance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/context-performance.md)
- **Adaptive Intelligence, Model Governance & Controlled Calibration**: [`docs/adaptive-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/adaptive-intelligence.md)
- **Model Governance Architecture**: [`docs/model-governance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/model-governance.md)
- **Prediction Method Versioning & Immutability**: [`docs/prediction-versioning.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-versioning.md)
- **Historical Backtesting & Calibration Engine**: [`docs/calibration-and-backtesting.md`](file:///home/izzatbek/Desktop/agronexsus/docs/calibration-and-backtesting.md)
- **Controlled Rollout & Emergency Rollback**: [`docs/controlled-rollout.md`](file:///home/izzatbek/Desktop/agronexsus/docs/controlled-rollout.md)
- **Predictive Model Evaluation & Continuous Intelligence**: [`docs/prediction-evaluation.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-evaluation.md)
- **Prediction Outcome Matching Engine**: [`docs/prediction-outcome-matching.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-outcome-matching.md)
- **Prediction Evaluation Metrics & Formulas**: [`docs/prediction-metrics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-metrics.md)
- **Prediction Quality & Confidence Calibration**: [`docs/prediction-quality.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-quality.md)
- **Predictive Farm Intelligence & Forecasting**: [`docs/predictive-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/predictive-intelligence.md)
- **Predictive Intelligence Architecture**: [`docs/prediction-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-architecture.md)
- **Forecast Metrics & Agronomic Formulas**: [`docs/forecast-metrics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/forecast-metrics.md)
- **Prediction Data Quality & Sufficiency**: [`docs/prediction-data-quality.md`](file:///home/izzatbek/Desktop/agronexsus/docs/prediction-data-quality.md)
- **Farm Performance, Historical Intelligence & Analytics**: [`docs/farm-performance.md`](file:///home/izzatbek/Desktop/agronexsus/docs/farm-performance.md)
- **Analytics & Historical Intelligence Architecture**: [`docs/analytics-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/analytics-architecture.md)
- **Analytics Metric Governance & Definitions**: [`docs/analytics-metrics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/analytics-metrics.md)
- **Decision-to-Action Operational Workflow Architecture**: [`docs/operational-workflow.md`](file:///home/izzatbek/Desktop/agronexsus/docs/operational-workflow.md)
- **Operations Center Workspace UX Guidelines**: [`docs/operations-ux.md`](file:///home/izzatbek/Desktop/agronexsus/docs/operations-ux.md)
- **Unified Farm Intelligence Dashboard & Action Center**: [`docs/dashboard-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/dashboard-architecture.md)
- **Agricultural UX & Action Center Guidelines**: [`docs/ux-guidelines.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ux-guidelines.md)
- **Telemetry Intelligence & Agronomic Recommendations**: [`docs/telemetry-intelligence.md`](file:///home/izzatbek/Desktop/agronexsus/docs/telemetry-intelligence.md)
- **Agronomic Recommendations Architecture**: [`docs/agronomic-recommendations.md`](file:///home/izzatbek/Desktop/agronexsus/docs/agronomic-recommendations.md)
- **Smart Irrigation Planning & Optimization Architecture**: [`docs/irrigation-planning.md`](file:///home/izzatbek/Desktop/agronexsus/docs/irrigation-planning.md)
- **Water Allocation & Budgeting Optimization**: [`docs/water-allocation.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-allocation.md)
- **Smart Irrigation REST API Contracts & Lifecycle**: [`docs/irrigation-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/irrigation-api.md)
- **Milestone 08.2 Implementation Plan**: [`docs/milestone-08.2-plan.md`](file:///home/izzatbek/Desktop/agronexsus/docs/milestone-08.2-plan.md)
- **AI Agronomic Recommendations & Decision Support**: [`docs/ai-agronomic-recommendations.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ai-agronomic-recommendations.md)
- **Recommendation REST API Contracts & Lifecycle**: [`docs/recommendation-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/recommendation-api.md)
- **Advisory AI Provider & Reasoning Architecture**: [`docs/ai-provider.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ai-provider.md)
- **Telemetry Anomaly Detection Architecture**: [`docs/anomaly-detection.md`](file:///home/izzatbek/Desktop/agronexsus/docs/anomaly-detection.md)
- **Configurable Anomaly Rules & Thresholds**: [`docs/anomaly-rules.md`](file:///home/izzatbek/Desktop/agronexsus/docs/anomaly-rules.md)
- **Alert Lifecycle, Deduplication & Management**: [`docs/alert-management.md`](file:///home/izzatbek/Desktop/agronexsus/docs/alert-management.md)
- **Anomaly REST API Contracts & Security**: [`docs/anomaly-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/anomaly-api.md)
- **Telemetry Analytics & Historical REST API**: [`docs/telemetry-analytics.md`](file:///home/izzatbek/Desktop/agronexsus/docs/telemetry-analytics.md)
- **Time-Series Storage & TimescaleDB Strategy**: [`docs/time-series-storage.md`](file:///home/izzatbek/Desktop/agronexsus/docs/time-series-storage.md)
- **Telemetry Aggregation Engine & Modality Policies**: [`docs/telemetry-aggregation.md`](file:///home/izzatbek/Desktop/agronexsus/docs/telemetry-aggregation.md)
- **Sensor Domain & Telemetry Foundation**: [`docs/sensor-domain.md`](file:///home/izzatbek/Desktop/agronexsus/docs/sensor-domain.md)
- **Sensor REST API Specification**: [`docs/sensor-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/sensor-api.md)
- **Sensor Telemetry & Simulation**: [`docs/sensor-abstraction.md`](file:///home/izzatbek/Desktop/agronexsus/docs/sensor-abstraction.md)
- **Water + GIS Intelligence Architecture**: [`docs/gis-water-integration.md`](file:///home/izzatbek/Desktop/agronexsus/docs/gis-water-integration.md)
- **GIS Water Intelligence REST API**: [`docs/gis-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/gis-api.md)
- **Water Management UI & Dashboards**: [`docs/water-frontend.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-frontend.md)
- **Water Management & Irrigation Domain**: [`docs/water-domain.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-domain.md)
- **Water Application Services & REST API**: [`docs/water-api.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-api.md)
- **Water Consumption & Efficiency Rules**: [`docs/water-consumption.md`](file:///home/izzatbek/Desktop/agronexsus/docs/water-consumption.md)
- **Field Domain & Parcel Architecture**: [`docs/field-domain.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-domain.md)
- **Field Authorization & Security Blueprint**: [`docs/field-security.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-security.md)
- **Field Frontend & UI Architecture**: [`docs/field-frontend.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-frontend.md)
- **Field GIS & Map Visualization**: [`docs/field-gis.md`](file:///home/izzatbek/Desktop/agronexsus/docs/field-gis.md)
- **Frontend Architecture & Design Guidelines**: [`docs/frontend-architecture.md`](file:///home/izzatbek/Desktop/agronexsus/docs/frontend-architecture.md)
- **Module Boundaries & Ownership**: [`docs/module-boundaries.md`](file:///home/izzatbek/Desktop/agronexsus/docs/module-boundaries.md)
- **Database Strategy & Migrations**: [`docs/database-strategy.md`](file:///home/izzatbek/Desktop/agronexsus/docs/database-strategy.md)
- **Authentication & Security Architecture**: [`docs/authentication.md`](file:///home/izzatbek/Desktop/agronexsus/docs/authentication.md)
- **AI Abstraction Strategy**: [`docs/ai-abstraction.md`](file:///home/izzatbek/Desktop/agronexsus/docs/ai-abstraction.md)

