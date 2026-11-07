# Milestone 08.13 Completion Report
# Farm-Level Predictive Risk & Resilience Intelligence

**Project:** YANGIARIQ AQUA AI  
**Milestone:** 08.13 — Farm-Level Predictive Risk & Resilience Intelligence  
**Author:** AI Engineering & Architecture Team  
**Date:** November 7, 2026  
**Status:** COMPLETE & FULLY VERIFIED  

---

## 1. Executive Summary

Milestone **08.13 — Farm-Level Predictive Risk & Resilience Intelligence** completes the transformation of YANGIARIQ AQUA AI from a fragmented data platform into an explainable, evidence-based agricultural decision-support engine.

By synthesizing real-time telemetry, hydraulic anomalies, volumetric water balances, operational irrigation calendars, and hybrid ML prediction health into a unified, deterministic intelligence layer, the platform answers the core operational questions vital to farmers and canal dispatchers in Yangiariq district (Khorezm, Uzbekistan):
1. **Which fields are currently at risk?** (Field Risk Matrix ranked by deterministic priority).
2. **What exactly is causing the risk?** (Multi-factor root-cause analysis with identified metrics).
3. **How serious is the situation?** (Explicit severity: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`, `UNKNOWN`).
4. **How reliable is the available evidence?** (Independent confidence: `HIGH`, `MEDIUM`, `LOW`, `INSUFFICIENT_DATA`).
5. **What could happen if no action is taken?** (Projected consequence and agronomic impact).
6. **What should the farmer or operator do next?** (Actionable recommendations deep-linked to existing workflows).
7. **Which actions should receive priority?** (Deterministic 9-tier precedence hierarchy with non-negotiable hydraulic safety dominance).

The implementation enforces zero autonomous actuator control, preserves all existing business logic, introduces zero duplicate prediction engines, and guarantees strict multi-tenant authorization (anti-IDOR).

---

## 2. Repository Audit Findings

Prior to implementation, a comprehensive audit of the entire codebase was conducted:
- **Module Boundaries & Domain Capabilities**:
  - `field`: Clean GIS parcel boundaries, crop profiles, soil types, and farmer ownership.
  - `water`: Volumetric allocation, consumption accounting, seasonal budgets, and water source catalog.
  - `sensor`: Hardware telemetry streams, time-series aggregation, freshness tracking, and quality flags (`VALID`, `SUSPECT`, `INVALID`).
  - `anomaly`: Statistical Z-score and multi-metric anomaly detection, pipe burst detection, and pressure drop alerts.
  - `prediction` (08.7–08.12): FAO-56 moisture trajectories, Ridge ML candidates, backtesting, model governance, and champion-challenger comparisons.
  - `irrigation` & `operations`: Smart irrigation plan state machine, constraint validation, 7-step operational timeline, and conflict detection.
  - `dashboard`: Read-model aggregation and centralized Action Center.
- **Audit Conclusions**:
  - Existing predictive, anomaly, and sensor modules provided rich domain data but lacked a unified intelligence layer to synthesize cross-cutting risks.
  - Prioritization across domains was needed to prevent low-priority agricultural advice from obscuring pipe rupture emergencies.
  - Missing data needed formal gating so that sensor failures would never be interpreted as healthy field conditions.

---

## 3. Implemented Capabilities

1. **Deterministic Risk Assessment Engine (`RiskAssessmentEngine`)**:
   - Synthesizes 6 risk categories: Hydraulic Safety, Water Availability, Soil Moisture, Irrigation Operational, Sensor & Data Reliability, and Predictive Model Reliability.
   - Computes non-linear composite risk score $S \in [0.00, 100.00]$ using $S_{\max} \times 0.70 + \sum S_{other} \times 0.15$.
   - Enforces absolute hydraulic safety override ($S = 100.00$ and `isHydraulicBlocked = true`).
2. **Confidence & Evidence Normalization Engine**:
   - Normalizes evidence into `OBSERVED`, `PREDICTED`, `UNRELIABLE`, and `MISSING`.
   - Strictly separates confidence from severity.
   - Discloses missing telemetry and model sample insufficiency ($N < 30$).
3. **Deterministic 9-Tier Prioritization Hierarchy**:
   - Ranks parcels and recommendations deterministically (1: Hydraulic safety $\to$ 9: Nominal baseline).
   - Generates human-readable explanatory rationale for every priority ranking.
4. **Actionable Risk Recommendation Synthesis**:
   - Creates targeted recommendations linking directly into existing execution workflows (`/operations`, `/sensors`, `/anomalies`, `/irrigation`).
5. **Human-in-the-Loop Assessment Lifecycle**:
   - Supports `OPEN`, `ACKNOWLEDGED`, `ACTION_REQUIRED`, `MITIGATED`, `RESOLVED`, `EXPIRED`.
   - Requires mandatory remedial audit notes for resolution.
6. **In-App Risk Notification Integration**:
   - Deduplicates alerts with a 30-minute cooldown and suppresses simulated broadcasts.
7. **Frontend Farm Risk Intelligence Center & Dashboard Integration**:
   - Complete responsive interface with overview cards, emergency banner, dual table/card views, factor breakdowns, evidence panels, trend charts, history timelines, and resolution modals.
   - Farm Risk Dashboard Card integrated directly into the main Dashboard.

---

## 4. Domain Architecture

The `risk` module is organized according to Clean Architecture:
```
backend/src/main/java/com/aquaai/risk/
├── api/                           # REST Controllers & DTOs
│   ├── RiskIntelligenceController.java
│   └── dto/                       # Request/Response records
├── application/                   # Use Cases & Coordinator
│   ├── service/RiskAssessmentCoordinatorService.java
│   └── usecase/                   # 8 Use cases (Overview, List, Profile, History, etc.)
├── domain/                        # Pure Domain Model & Services (Zero Spring/JPA dependencies)
│   ├── model/                     # FarmRiskAssessment, RiskFactor, RiskEvidence, etc.
│   ├── port/                      # Outbound SPI ports to field, water, sensor, anomaly, etc.
│   ├── repository/                # RiskAssessmentRepository interface
│   └── service/RiskAssessmentEngine.java
└── infrastructure/                # Adapters & Persistence
    ├── adapter/                   # Integration adapters for other modules
    ├── notification/              # InAppRiskNotificationAdapter
    └── persistence/               # JPA entity, mapper, Spring Data repository
```

---

## 5. Risk Calculation Rules & Formulas

- **Composite Score Formula**:
  $$S_{composite} = \begin{cases} 100.00 & \text{if hydraulic safety blocked} \\ 0.00 & \text{if factors are empty} \\ \min\left(100.00, S_{\max} \times 0.70 + \sum S_{\text{other}} \times 0.15\right) & \text{otherwise} \end{cases}$$
- **Thresholds**:
  - Hydraulic: $P_{line} < 0.50$ bar (Critical Collapse), $P_{line} < 1.20$ bar (Sub-optimal).
  - Soil Moisture: $< 15.0\%$ (Permanent Wilting Point), $< 20.0\%$ (Deficit), $> 35.0\%$ (Waterlogging), drying rate $< -0.15\%/\text{hr}$ (Rapid).
  - Water Quota: $\le 0\text{ m}^3$ (Exhausted), $V_{remaining} < V_{upcoming}$ (Deficit), $\ge 90.0\%$ (High Utilization).
  - Telemetry: $> 6\text{h}$ (Stale), $\ge 2$ offline (High Risk).
  - Model: `DEGRADED` health, contextual drift active, $N < 30$ outcomes.

---

## 6. Evidence and Confidence Model

- **Evidence Taxonomy**:
  - `OBSERVED`: Ground-truth physical telemetry or database state.
  - `PREDICTED`: Machine learning or statistical trajectory forecast.
  - `UNRELIABLE`: Stale feed ($>6\text{h}$), suspect quality, or degraded model.
  - `MISSING`: Absent domain data; triggers missing data warnings.
- **Orthogonality**: Severity (`CRITICAL`..`LOW`) reflects real-world impact; Confidence (`HIGH`..`INSUFFICIENT_DATA`) reflects evidentiary certainty.

---

## 7. Backend API Endpoints

All endpoints follow RESTful conventions, DTO boundaries, and standard response wrappers (`ApiResponse<T>`):

| Method | Endpoint | Description | Roles |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/risk-intelligence/overview` | Farm-level aggregated risk KPIs and emergency banners | FARMER, OPERATOR, ADMIN |
| `GET` | `/api/v1/risk-intelligence/fields` | Paginated, filtered parcel risk profiles | FARMER, OPERATOR, ADMIN |
| `GET` | `/api/v1/risk-intelligence/fields/{fieldId}` | Deep parcel risk profile with factors and evidence | FARMER, OPERATOR, ADMIN |
| `GET` | `/api/v1/risk-intelligence/fields/{fieldId}/history` | Historical risk assessment audit trail | FARMER, OPERATOR, ADMIN |
| `GET` | `/api/v1/risk-intelligence/recommendations` | Paginated prioritized actionable recommendations | FARMER, OPERATOR, ADMIN |
| `GET` | `/api/v1/risk-intelligence/recommendations/{id}` | Detailed recommendation inspection | FARMER, OPERATOR, ADMIN |
| `POST` | `/api/v1/risk-intelligence/assessments/{id}/acknowledge`| Acknowledge risk assessment | FARMER (owner), OPERATOR, ADMIN |
| `POST` | `/api/v1/risk-intelligence/assessments/{id}/resolve` | Resolve assessment with mandatory notes | FARMER (owner), OPERATOR, ADMIN |

---

## 8. Frontend Components & Pages

Located under `frontend/src/features/risk/`, `frontend/src/entities/risk/`, and `frontend/src/pages/risks/`:
- **RiskOverviewCards**: 6 interactive KPI cards with real-time filtering.
- **CriticalRiskBanner**: Prominent alert on hydraulic emergencies.
- **FieldRiskTable & FieldRiskCard**: Dual grid/table parcel views with composite score bars.
- **RiskSeverityBadge & RiskConfidenceIndicator**: Clear visual badges.
- **RiskFactorBreakdown**: Detailed factor explanations with causes, consequences, and missing warnings.
- **RiskEvidencePanel**: Distinct grouping by `OBSERVED`, `PREDICTED`, `UNRELIABLE`, and `MISSING`.
- **RiskTrendChart & RiskHistoryTimeline**: Historical score progression.
- **RiskRecommendationCard**: Prioritized action cards with direct workflow navigation.
- **RiskAssessmentDetailModal**: Full parcel drilldown modal.
- **RiskActionPanel**: Human-in-the-loop acknowledgement and resolution modal with mandatory notes.
- **FarmRiskDashboardCard**: Embedded situational awareness widget in main Dashboard.

---

## 9. Security & Authorization

- **Unauthenticated Access**: All endpoints strictly require JWT Bearer authentication (401 Unauthorized).
- **Anti-IDOR Guarantees**: Farmers can only view and act upon fields they own. Foreign field lookups return `404 Not Found` (anti-enumeration).
- **Audit Logging**: Resolution user ID, timestamp, and remedial notes are permanently recorded.

---

## 10. Safety Guarantees

1. AI models cannot directly actuate pumps, valves, or gates.
2. Recommendations cannot bypass human operator or farmer approval.
3. Hydraulic safety interlocks hold absolute precedence over all agricultural and economic scores.
4. Missing telemetry is never treated as a safe condition.
5. Predictions are never displayed as observed facts.
6. Degraded models automatically lower predictive confidence.

---

## 11. Database Changes

Liquibase changeset `021-create-risk-intelligence-tables.sql` added:
- Table `farm_risk_assessments`: Stores parcel assessments, composite scores, hydraulic flags, factor JSON, recommendation JSON, missing warnings JSON, and resolution audit fields.
- Indexes:
  - `idx_risk_assessments_field_evaluated` on `(field_id, evaluated_at DESC)`.
  - `idx_risk_assessments_status` on `(lifecycle_status)`.
  - `idx_risk_assessments_severity` on `(overall_severity)`.
  - `idx_risk_assessments_evaluated` on `(evaluated_at DESC)`.

---

## 12. Verification & Test Results

### Backend Tests
- **Total Backend Tests Executed:** **704 tests**
- **Failures:** **0**
- **Status:** **BUILD SUCCESSFUL**
- **Test Categories:**
  - `RiskAssessmentEngineTest`: 15 unit tests verifying factor calculations, hydraulic precedence, composite scoring, missing-data gating, and priority ranking.
  - `RiskAssessmentCoordinatorServiceTest`: Context collection and domain orchestration.
  - `RiskIntelligenceSecurityIntegrationTest`: 10 integration tests validating 401 unauthenticated access, Anti-IDOR field isolation, foreign field 404s, farmer vs operator vs admin permissions, and lifecycle state changes.
  - `ArchitectureTests`: ArchUnit tests #77–82 verifying package isolation, absence of JPA/Spring dependencies in domain, and absence of hardware/actuator SDKs.

### Frontend Tests
- **Total Frontend Test Suites Executed:** **25 files**
- **Total Frontend Tests Executed:** **223 tests**
- **Failures:** **0**
- **Status:** **ALL PASSED**
- **Test Suite `farmRiskIntelligence.test.tsx`:** 13 comprehensive component and integration tests covering:
  1. `RiskSeverityBadge` visual states across all severities.
  2. `RiskConfidenceIndicator` separating confidence from severity and handling `INSUFFICIENT_DATA`.
  3. `CriticalRiskBanner` alerting and locking pressurization on hydraulic interlocks.
  4. `RiskOverviewCards` rendering and interactive category filtering.
  5. `FieldRiskCard` displaying composite score, priority rank, and parcel agronomic attributes.
  6. `FieldRiskTable` search, severity filtering, and hydraulic badges.
  7. `RiskEvidencePanel` categorizing `OBSERVED`, `PREDICTED`, `UNRELIABLE`, and `MISSING` evidence.
  8. `RiskFactorBreakdown` explaining contributions, consequences, and missing inputs.
  9. `RiskRecommendationCard` rendering priority rank, triggers, and deep links.
  10. `RiskActionPanel` enforcing human approval and mandatory resolution audit notes.
  11. `RisksPage` full tab switching and query integration.
  12. `FarmRiskDashboardCard` dashboard integration and navigation.
  13. `RiskAssessmentDetailModal` full modal rendering and tab inspection.

---

## 13. Build, Lint & Typecheck Results

| Quality Gate | Tool / Command | Exit Code | Result |
| :--- | :--- | :--- | :--- |
| **Backend Build** | `./gradlew test --rerun-tasks` | `0` | **BUILD SUCCESSFUL** (704/704 passed) |
| **Frontend Typecheck** | `npm run typecheck` (`tsc -b`) | `0` | **Clean** (0 errors) |
| **Frontend Lint** | `npm run lint` (`eslint .`) | `0` | **Clean** (0 errors) |
| **Frontend Tests** | `npm test` (`vitest run`) | `0` | **Clean** (223/223 passed) |
| **Frontend Production Build** | `npm run build` (`vite build`) | `0` | **Clean** (Production bundle generated) |

---

## 14. Documentation Changes

The following new documentation files have been created:
- [`docs/farm-risk-intelligence.md`](farm-risk-intelligence.md)
- [`docs/risk-assessment-rules.md`](risk-assessment-rules.md)
- [`docs/risk-confidence-and-evidence.md`](risk-confidence-and-evidence.md)
- [`docs/milestone-08.13-completion-report.md`](milestone-08.13-completion-report.md)

The following existing documentation files have been updated:
- [`README.md`](../README.md)
- [`ARCHITECTURE.md`](../ARCHITECTURE.md)
- [`docs/module-boundaries.md`](module-boundaries.md)
- [`docs/frontend-architecture.md`](frontend-architecture.md)
- [`docs/predictive-intelligence.md`](predictive-intelligence.md)
- [`docs/dashboard-architecture.md`](dashboard-architecture.md)

---

## 15. Known Limitations

1. **Weather Forecast Dependency**: Soil moisture forward depletion modeling currently derives from simulated evapotranspiration rates and historical regression when real-time local micro-weather station feeds are offline.
2. **In-Memory Notification Cooldown**: The deduplication cache in `InAppRiskNotificationAdapter` resides in application memory; multi-instance horizontal scaling would benefit from distributed cache backing (e.g., Redis) when deployed across multi-pod clusters.
3. **Soil Salinity Telemetry**: While soil moisture and hydraulic pressure are monitored in real time, electrical conductivity (EC) sensors are currently simulated awaiting physical sensor deployment.

---

## 16. Recommended Next Milestone

- **Milestone 08.14 — Regional Water Distribution Network Resilience & Automated Canal Dispatch Optimization**:
  - Expanding from field-parcel scale to district-wide primary/secondary canal network topology.
  - Multi-parcel cooperative scheduling to eliminate secondary canal contention across Yangiariq WUA (Water Users Association) boundaries.
  - Hydraulic surge protection modeling for variable-frequency pump stations.
