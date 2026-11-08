# YANGIARIQ AQUA AI

> **Smart Water-Management Decision Support Platform for Yangiariq District**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue.svg)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-5.4-purple.svg)](https://vitejs.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Liquibase](https://img.shields.io/badge/Liquibase-Managed-red.svg)](https://www.liquibase.org/)

---

## Overview

**Yangiariq Aqua AI** is an agricultural technology platform engineered to deliver decision support for water distribution operators and local farmers in Yangiariq district (Khorezm, Uzbekistan).

The platform addresses regional challenges in canal water scheduling, soil salinization risk, and irrigation optimization through:
- **Regional Water Operations Performance, Outcome Measurement & Evidence-Based Impact Assessment (Milestone 08.17)**: Authoritative outcome measurement and impact assessment layer (`com.aquaai.performance`) connecting intelligence modules to evaluate physical reality:
  - Tripartite conceptual separation: strictly separates OUTPUT (activities/schedules) from OUTCOME (measured changes in physical state) from IMPACT (conservative, verified causal attribution).
  - Deterministic performance indicator catalog across delivery reliability, conveyance efficiency, allocation compliance, distribution equity ($CV$), irrigation efficiency, incident recurrence, data quality, and verified conservation savings.
  - Robust 5-point baseline comparability engine (`BaselineComparabilityEngine`) enforcing duration ratio (0.50–2.00), unit compatibility, geographic scope alignment, seasonal/climatic context matching, and $\ge 80\%$ completeness.
  - Conservative attribution hierarchy (`DIRECTLY_VERIFIED`, `STRONGLY_ASSOCIATED`, `ASSOCIATED`, `INCONCLUSIVE`, `NOT_ASSESSABLE`) with strict invariant: simulated or estimated baselines can NEVER yield directly verified attribution.
  - Non-actuation guarantee: purely observational, analytical, and governance layer with zero direct actuator control (ArchUnit Rule 106).
  - Comprehensive frontend workspace: overview KPI cards, dynamic trend charts with visual missing data breaks and simulated badges, indicator catalog, impact assessment registry with independent auditor review, and baseline/target management.
  - Multi-tenant anti-IDOR security preventing cross-farm data leakage. Guarded by 106 ArchUnit rules, 836 backend tests (820 passed, 0 failures, 16 skipped), and 275 frontend tests across 29 test files.
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
- **Regional Water Accounting, Distribution Equity & Conservation Intelligence (Milestone 08.15)**: Authoritative regional water accounting layer connecting planned water distribution, actual canal delivery, field consumption, conveyance losses, allocation compliance, and inter-cluster fairness:
  - Universal mass-conservation water balance ($Closing = Opening + Inflow - Consumed - Losses - ReturnFlow$) with automated tolerance discrepancy detection.
  - Traceable accounting chain preventing double-counting between source abstraction, canal transfer, parcel turnout delivery, and crop root-zone consumption.
  - Continuous conveyance loss accounting on canal reaches with upstream-downstream differential analysis and physical classification (`ACCEPTABLE_CONVEYANCE`, `VERIFIED_LOSS`, `SUSPECTED_LOSS`, `UNACCOUNTED_WITHDRAWAL`, `MEASUREMENT_DISCREPANCY`).
  - Parcel-level quota allocation compliance tracking with delivery shortfall and over-delivery alerts.
  - Statistical distribution equity analysis ($m^3/\text{ha}$, sample variance, coefficient of variation $CV$, and rotational delivery delay hours) across Water User Associations (WUAs) to eliminate tail-end disadvantages.
  - Multi-stage boundary reconciliation with human operator review workflow and immutable audit log notes.
  - Evidence-based water conservation intelligence providing verifiable recommendations and engineering recovery calculations (guaranteeing zero fabricated savings).
  - Modern responsive frontend with mass-balance visualization, searchable transaction ledger, equity comparisons, and field accounting drill-down.
- **Regional Water Distribution Network Resilience & Canal Dispatch Optimization (Milestone 08.14)**: District-scale water conveyance network intelligence, topological reachability validation, and coordinated multi-parcel canal dispatch optimization. Features:
  - Directed graph network modeling connecting river intakes (Amu Darya branch / Shavat canal), primary/secondary canals, control gates, booster pump stations, Water User Associations (WUAs), and parcel inlets.
  - Automated graph topology validation detecting disconnected nodes, cycles, and capacity inconsistencies.
  - Transparent fairness optimization policies (`CROP_CRITICALITY_FIRST`, `MOISTURE_DEFICIT_PRIORITY`, `PROPORTIONAL_DEFICIT`) resolving multi-parcel water competition while staggering delivery slots to prevent peak canal surcharging.
  - Non-negotiable hydraulic safety dominance: Hard safety interlocks immediately block deliveries upon active pipe bursts or critical pressure drops.
  - Zero autonomous actuation guarantee: The software provides decision-support and planning only; ArchUnit Test 88 prevents inclusion of hardware actuation libraries. All physical gate and pump adjustments require authorized human review and execution.
  - Isolated "What-If" resilience stress testing simulating canal breaches, pump station outages, contamination, and drought restrictions strictly in-memory without mutating operational database records.
  - Actual delivery accounting, variance tracking, and conveyance loss analysis.
  - Interactive Leaflet GIS water network map using open OpenStreetMap tiles (zero external paid providers / zero API keys).
- **Farm-Level Predictive Risk & Resilience Intelligence (Milestone 08.13)**: Evidence-based agricultural risk intelligence synthesizing physical telemetry, hydraulic anomalies, water balances, and model health into explainable risk profiles. Answers: *Which fields are at risk? What causes it? How serious is it? How reliable is the evidence? What could happen if no action is taken? What should be done next? Which actions receive priority?* Enforces non-negotiable hydraulic safety dominance, composite risk scoring, independent severity/confidence matrix, deterministic 9-tier precedence hierarchy, Anti-IDOR security, and human-in-the-loop lifecycle (`OPEN` $\to$ `ACKNOWLEDGED` $\to$ `ACTION_REQUIRED` $\to$ `MITIGATED` $\to$ `RESOLVED` $\to$ `EXPIRED`).
- **Hybrid ML Prediction Engine & Evidence-Based Learning (Milestone 08.11)**: Introduces controlled, pluggable supervised regression candidates that compete with the deterministic agronomic baseline using verified historical ground-truth. Enforces a non-negotiable safety principle: *ML predicts, deterministic agronomic and hydraulic rules constrain, human governance decides production activation*. Features in-process JVM-native Ridge regression, bounded residual adjustments (max $\pm 5.0\%$), reproducible dataset snapshots with SHA-256 fingerprints, strict 60/20/20 chronological train/validation/test temporal splits (zero future leakage), evidence-based sample gating ($N \ge 30$), automated regression guards, and full integration into the 9-level context-aware adaptive selection hierarchy.
- **Adaptive Intelligence, Model Governance & Controlled Calibration**: Continuous model governance lifecycle moving from degradation detection to controlled calibration: Insight $\to$ Proposal $\to$ Backtest $\to$ Human Review $\to$ Version $\to$ Controlled Rollout $\to$ Monitoring $\to$ Rollback. Enforces zero autonomous self-modification, permanent immutability of hard hydraulic safety parameters, anti-leakage historical backtesting, multi-factor regression guarding, staged rollout (`SHADOW`, `CANARY`, `ACTIVE`), and 1-click emergency rollback with complete auditability.
- **Predictive Model Evaluation & Continuous Intelligence**: Empirical verification and continuous intelligence feedback loop answering *"How accurate was that prediction?", "Where did it succeed/fail?", "Is the model drifting over time?"*. Features arbitrary-precision regression and classification metrics, safety interlock outcome interruption classification, minimum sample size gating ($n \ge 10$), and temporal performance drift detection.
- **Predictive Farm Intelligence & Forecasting**: Forward-looking decision support forecasting soil moisture trajectories, irrigation need likelihood, water demand volume, allocation headroom risk, and telemetry reliability across standardized horizons (`NEXT_24_HOURS`, `NEXT_48_HOURS`, `NEXT_7_DAYS`).
- **Farm Performance, Historical Intelligence & Analytics**: Historical intelligence workspace answering *"What happened before?", "What changed?", "How well did we perform?", "What patterns can we see?", "What should we improve?"*. Features water quota utilization vs plan variance governance, chronological trend projections, explainable field ranking indices with safety dominance, data confidence auditing, period-over-period comparisons, and deterministic actionable insights deep-linking into execution workflows.
- **Decision-to-Action Operations Center**: Continuous operational workspace moving from problem detected to verified field action with minimal clicks. Features deterministic 7-step workflow timeline, telemetry situation cards, precedence constraint checks, human approval with mandatory constraint acknowledgement, manual field execution logging, and post-irrigation closed-loop verification.
- **Farm Intelligence Workspace & Action Center**: Unified 10-second operational overview, deterministic parcel priority scoring, explainable rationale dialogs, centralized action center with one-click resolution CTAs, volumetric water budget breakdown, and 6-stage irrigation lifecycle tracking.
- **Farmer & Field Management**: Geospatial mapping of parcels, crop types, and soil parameters.
- **Irrigation Auditing**: Quantitative records of water volume delivered per field.
- **Incident Reporting**: Real-time reporting of canal blockages, leakages, and water disputes.
- **AI-Powered Decision Support**: Decoupled advisory model leveraging Google Gemini for drought prevention and irrigation scheduling.
- **Hardware-Decoupled Telemetry**: Sensor abstraction enabling simulated soil moisture and canal flow metrics today, ready for physical IoT gateways tomorrow.
- **Multilingual Support**: Uzbek (`uz`), Russian (`ru`), and English (`en`).

---

## Architectural Highlights

- **Modular Monolith**: Clean boundaries between business domains (`auth`, `user`, `field`, `water`, `problem`, `dashboard`, `ai`, `sensor`, `anomaly`, `recommendation`, `irrigation`, `intelligence`, `operations`, `analytics`, `prediction`, `risk`, `network`, `wateraccounting`, `accountability`, `notification`, `shared`).
- **Clean Package-by-Feature**: Explicit separation between `api`, `application`, `domain`, and `infrastructure` layers.
- **Architectural Guardrails**: Enforced via **ArchUnit** tests ensuring that controllers never access repositories or bypass DTO mappings.
- **Pluggable AI Port**: `AiProvider` interface supporting Gemini API and mock fallbacks without leaking vendor SDK dependencies into domain logic.
- **Pluggable Sensor Port**: `SensorProvider` with `SimulatedSensorProvider` for realistic agricultural telemetry without requiring physical sensors.

For deep architectural details, see [ARCHITECTURE.md](ARCHITECTURE.md) and [docs/](docs/).

---

## Quick Start

### 1. Prerequisites
- Docker & Docker Compose
- Java 21 OpenJDK
- Node.js 20+

### 2. Configure Environment
```bash
cp .env.example .env
```

### 3. Start Database
```bash
docker compose up -d postgres
```

### 4. Run Backend
```bash
cd backend
./gradlew bootRun
```
Access Swagger UI at `http://localhost:8080/swagger-ui.html`.

### 5. Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Access Frontend at `http://localhost:5173`.

### 6. Run Complete Stack in Docker
```bash
docker compose up --build -d
```
- Frontend UI: `http://localhost:3000`
- Backend API: `http://localhost:8080`

---

## Seed Accounts (Development)

| Role | Username / Phone | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Admin** | `+998901234567` | `admin123` | Yangiariq District Super Admin |
| **Operator** | `+998907654321` | `admin123` | Canal Dispatch Operator |

---

## Documentation Index

- [Regional Water Network Architecture](docs/water-network-architecture.md)
- [Network Topology & Capacity Engine](docs/network-topology-and-capacity.md)
- [Canal Dispatch Optimization & Fairness Policies](docs/canal-dispatch-optimization.md)
- [Cooperative Irrigation Scheduling](docs/cooperative-irrigation-scheduling.md)
- [Network Resilience & What-If Scenarios](docs/network-resilience-scenarios.md)
- [Hydraulic Safety Limitations & Zero Actuation](docs/hydraulic-safety-limitations.md)
- [Milestone 08.14 Completion Report](docs/milestone-08.14-completion-report.md)
- [Farm-Level Predictive Risk & Resilience Intelligence](docs/farm-risk-intelligence.md)
- [Risk Assessment Rules & Prioritization Formulas](docs/risk-assessment-rules.md)
- [Risk Confidence & Evidence Model](docs/risk-confidence-and-evidence.md)
- [Milestone 08.13 Completion Report](docs/milestone-08.13-completion-report.md)
- [Context-Aware Prediction Engine](docs/context-aware-prediction.md)
- [Adaptive Method Selection & 9-Level Hierarchy](docs/adaptive-selection.md)
- [Context Performance & Local Drift Detection](docs/context-performance.md)
- [Adaptive Intelligence Architecture](docs/adaptive-intelligence.md)
- [Model Governance Architecture](docs/model-governance.md)
- [Prediction Method Versioning & Immutability](docs/prediction-versioning.md)
- [Historical Backtesting & Calibration Engine](docs/calibration-and-backtesting.md)
- [Controlled Rollout & Rollback](docs/controlled-rollout.md)
- [Predictive Model Evaluation & Continuous Intelligence](docs/prediction-evaluation.md)
- [Prediction Outcome Matching Semantics](docs/prediction-outcome-matching.md)
- [Prediction Evaluation Metrics & Statistical Formulation](docs/prediction-metrics.md)
- [Prediction Data Quality, Gating & Performance Drift](docs/prediction-quality.md)
- [Predictive Farm Intelligence & Forecasting](docs/predictive-intelligence.md)
- [Predictive Intelligence Architecture](docs/prediction-architecture.md)
- [Forecast Metrics & Agronomic Formulas](docs/forecast-metrics.md)
- [Prediction Data Quality & Sufficiency](docs/prediction-data-quality.md)
- [Farm Performance & Analytics Workspace](docs/farm-performance.md)
- [Unified Farm Intelligence Dashboard & Action Center](docs/dashboard-architecture.md)
- [Agricultural UX & Action Center Guidelines](docs/ux-guidelines.md)
- [Architecture Blueprint](ARCHITECTURE.md)
- [Architecture Overview](docs/architecture-overview.md)
- [Water + GIS Intelligence Architecture](docs/gis-water-integration.md)
- [GIS Water Intelligence REST API](docs/gis-api.md)
- [Water Frontend & Consumption Dashboards](docs/water-frontend.md)
- [Water Domain & Accounting Rules](docs/water-domain.md)
- [Water Consumption & Efficiency](docs/water-consumption.md)
- [Field GIS & Cartography](docs/field-gis.md)
- [Module Boundaries & Ownership](docs/module-boundaries.md)
- [AI Abstraction Strategy](docs/ai-abstraction.md)
- [Sensor Domain & Telemetry Foundation](docs/sensor-domain.md)
- [Sensor & Telemetry REST API](docs/sensor-api.md)
- [Telemetry Analytics & Historical REST API](docs/telemetry-analytics.md)
- [Time-Series Storage & TimescaleDB Strategy](docs/time-series-storage.md)
- [Telemetry Aggregation Engine & Modality Policies](docs/telemetry-aggregation.md)
- [AI Agronomic Recommendations & Decision Support](docs/ai-agronomic-recommendations.md)
- [Recommendation REST API & Lifecycle](docs/recommendation-api.md)
- [Advisory AI Provider & Reasoning Architecture](docs/ai-provider.md)
- [Telemetry Intelligence & Agronomic Recommendations](docs/telemetry-intelligence.md)
- [Agronomic Recommendations Architecture](docs/agronomic-recommendations.md)
- [Smart Irrigation Planning & Optimization](docs/irrigation-planning.md)
- [Water Allocation & Budgeting Optimization](docs/water-allocation.md)
- [Smart Irrigation REST API Contracts](docs/irrigation-api.md)
- [Telemetry Anomaly Detection Architecture](docs/anomaly-detection.md)
- [Configurable Anomaly Rules & Thresholds](docs/anomaly-rules.md)
- [Alert Lifecycle, Deduplication & Management](docs/alert-management.md)
- [Anomaly REST API Contracts & Security](docs/anomaly-api.md)
- [Sensor Abstraction Strategy](docs/sensor-abstraction.md)
- [API Conventions](docs/api-conventions.md)
- [Database & Liquibase Strategy](docs/database-strategy.md)
- [Frontend Architecture](docs/frontend-architecture.md)
- [Development Workflow](docs/development-workflow.md)

---

## Engineering Standards

- **Code Quality**: SOLID, DRY, KISS, Separation of Concerns.
- **No Monolithic Bloat**: Granular domain services and DTOs.
- **Security**: No secrets or passwords in source control or application logs.
