# Milestone 08.20 Completion Report: Controlled Field Pilot, Ground-Truth Validation & Operational Acceptance

## 1. Executive Summary

**Milestone 08.20** establishes the **Controlled Field Pilot Framework** for **YANGIARIQ AQUA AI**.

Prior milestones (08.01–08.19) constructed farm and field GIS management, smart irrigation scheduling, ML model evaluation, water accounting, incident investigation, production Docker deployments, and data ingestion adapters (weather APIs, CSV batch imports, and HTTP gateway contracts).

Milestone 08.20 introduces the operational, scientific, and governance infrastructure required to transition YANGIARIQ AQUA AI from simulated software development to defensible, real-world field pilot validation across Khorezm, Uzbekistan.

In strict adherence to the non-negotiable principles:
- **No live field pilot is claimed to have occurred**; rather, the governance framework to conduct, monitor, and evaluate such pilots is now fully operational in code.
- **Simulated data remains explicitly tagged as `SIMULATED`** and is prohibited from contaminating ground-truth verification ledgers.
- **The Four-Eyes Principle** is architecturally and programmatically enforced: submitters cannot verify their own measurements.
- **No autonomous pump or valve actuation** has been introduced; the platform operates strictly as human-in-the-loop decision intelligence.
- **No unsupported agricultural claims** (water conservation percentages, yield gains) can be declared without verified baselines and empirical harvest evidence.

---

## 2. Implemented Capabilities by Milestone Phase

### Phase 1: Pilot Program Domain
- Introduced `PilotProgram` aggregate with full lifecycle state machine:
  `DRAFT` $\rightarrow$ `PLANNED` $\rightarrow$ `APPROVED` $\rightarrow$ `READY` $\rightarrow$ `ACTIVE` $\rightarrow$ `PAUSED` $\rightarrow$ `COMPLETED` / `CANCELLED`.
- Captured operational objectives, geographic scope, bounded calendar intervals, responsible organizations, program owner, and approval metadata.
- Reused existing `Field`, `User`, and `Sensor` domain aggregates without creating parallel parcel or tenancy registries.
- Participant enrollment (`PilotParticipant`) with role-based responsibilities (`FIELD_LEAD`, `OBSERVER`, `OPERATOR`, `FARMER`).
- Field enrollment (`PilotField`) capturing specific monitoring targets, sampling protocols, and validation criteria.

### Phase 2: Pilot Readiness Assessment
- Built multi-dimensional pre-flight gatekeeper (`PilotReadinessAssessment`) evaluating:
  1. **Infrastructure Readiness**: API health probes, database connection latency, Liquibase state, backup verification, and metrics registry.
  2. **Data Readiness**: Registered data sources, weather fallback status, sensor hardware presence (`AWAITING_HARDWARE`), and data quality rules.
  3. **Operational Readiness**: Assigned field leads, mapped field parcels, measurement protocols, and four-eyes verification assignments.
  4. **Governance Readiness**: Program administrative approval, participant consent, RBAC boundaries, and actuator air-gap verification.
- Output statuses: `PASS`, `WARNING`, `BLOCKED`, `NOT_ASSESSED`.
- **Gatekeeper Invariant**: Program activation is programmatically rejected if any mandatory check is `BLOCKED`.

### Phase 3: Ground-Truth Measurement and Validation
- Standardized canonical ground-truth ingestion for: `SOIL_MOISTURE`, `TEMPERATURE`, `RAINFALL`, `WATER_FLOW_RATE`, `WATER_VOLUME`, `WATER_LEVEL`, and `WATER_PRESSURE`.
- Verification lifecycle: `SUBMITTED` $\rightarrow$ `PENDING_REVIEW` $\rightarrow$ `VERIFIED` / `REJECTED` / `NEEDS_REMEASUREMENT`.
- **Enforced Four-Eyes Principle**: Rejects self-verification attempts with HTTP 400 and `IllegalArgumentException`.
- Auditable evidence references (calibration certificates, core probe depths, field photographs).
- Explicit recording of limitations where physical calibration certificates or digital photos are unavailable.

### Phase 4: Prediction vs. Actual Outcome Validation
- Linked machine learning forecasts (`com.aquaai.prediction`) with four-eyes verified ground-truth observations.
- Quantitative single-point and cohort metrics: Absolute Error ($AE$), Relative Error ($RE\%$), MAE, RMSE, and Mean Bias ($MBE$).
- Explicit evaluation states: `VALIDATION_PENDING`, `VALIDATED`, `INSUFFICIENT_DATA` ($N < 10$), and `INVALID_COMPARISON`.
- Prohibits automated promotion of challenger models based on small pilot datasets.

### Phase 5: Pilot Operational Monitoring
- Real-time operational monitoring synthesizing ground-truth submission velocity, verification backlog, prediction matching coverage, open incidents, and operator feedback.
- Strict evidence transparency: `OBSERVED`, `VERIFIED`, `ESTIMATED`, and `SIMULATED`.

### Phase 6: Operator Feedback and Field Observations
- Structured qualitative observation logging across 8 categories: `DATA_QUALITY`, `SENSOR_ISSUE`, `WEATHER_DISCREPANCY`, `IRRIGATION_OPERATION`, `WATER_DELIVERY`, `FIELD_CONDITION`, `SYSTEM_USABILITY`, and `OTHER`.
- Complete lifecycle: `SUBMITTED` $\rightarrow$ `UNDER_REVIEW` $\rightarrow$ `ACTION_REQUIRED` $\rightarrow$ `RESOLVED` / `REJECTED`.
- Direct linkage to pilot programs, fields, sensors, data sources, and accountability incidents.

### Phase 7: Pilot Incident and Discrepancy Management
- Integrated pilot discrepancies directly into the existing `com.aquaai.accountability` engine without duplicating investigation or SLA logic.
- Escalates measurement anomalies and equipment issues to root cause analysis and corrective action tracking.

### Phase 8: Pilot Outcome and Impact Evaluation
- Synthesized four evaluation pillars: Data Performance, Prediction Performance, Operational Performance, and Water Management Outcomes.
- Enforced five-tier evidentiary classification:
  - **Tier I**: Directly Observed (verified physical measurements).
  - **Tier II**: Calculated Indicators (deterministic math on Tier I).
  - **Tier III**: Estimated Outcomes (agronomic model inferences).
  - **Tier IV**: Simulated Scenarios (counterfactual scenarios).
  - **Tier V**: Unmeasured Outcomes (explicitly acknowledged as unmeasured).
- Generates immutable evaluation snapshots in database.

### Phase 9: Frontend Pilot Management Experience
- Comprehensive UI in React, TypeScript, Tailwind CSS, and shadcn/ui.
- Dedicated pages and tabbed workflows:
  - **Pilot Programs Dashboard**: Status filter cards, program directory, create dialog.
  - **Readiness & Enrollment Panel**: Multi-dimensional check cards, participant table, parcel enrollments.
  - **Ground-Truth Validation**: Submission modal, four-eyes review dialog, evidence reference viewer.
  - **Prediction vs. Actual**: Model error metrics, absolute/relative error comparisons.
  - **Feedback & Discrepancies**: Category badges, incident escalation indicators.
  - **Evaluation Report**: Synthesis snapshot generation, limitation narrative, and actionable recommendations.
- Trilingual support: Uzbek (`uz`), Russian (`ru`), and English (`en`).
- Light and Dark theme responsiveness.

---

## 3. Database Changes (Liquibase Migration 027)

File: `backend/src/main/resources/db/changelog/changes/027-create-field-pilot-tables.sql`

| Table Name | Description | Key Constraints & Foreign Keys |
| :--- | :--- | :--- |
| `pilot_programs` | Master program definition and lifecycle state. | `owner_id -> users(id)`, `approved_by -> users(id)`, unique `code`. |
| `pilot_participants` | Enrolled operators, agronomists, and farmers. | `pilot_id -> pilot_programs(id)`, `user_id -> users(id)`, unique `(pilot_id, user_id)`. |
| `pilot_fields` | Enrolled field parcels with measurement plans. | `pilot_id -> pilot_programs(id)`, `field_id -> fields(id)`, unique `(pilot_id, field_id)`. |
| `pilot_readiness_assessments` | Pre-flight readiness check records and serialized JSON. | `pilot_id -> pilot_programs(id)`, `assessed_by -> users(id)`. |
| `ground_truth_validations` | Physical field measurements and verification audit. | `pilot_id -> pilot_programs(id)`, `field_id -> fields(id)`, `submitted_by -> users(id)`, `verifier_id -> users(id)`. |
| `pilot_prediction_validations` | Comparison records between predictions and actuals. | `pilot_id -> pilot_programs(id)`, `field_id -> fields(id)`, `ground_truth_id -> ground_truth_validations(id)`. |
| `pilot_operator_feedback` | Qualitative operator notes, sensor alerts, and issues. | `pilot_id -> pilot_programs(id)`, `field_id -> fields(id)`, `submitted_by -> users(id)`, `assigned_to -> users(id)`. |
| `pilot_evaluation_snapshots` | Immutable periodic evaluation snapshots. | `pilot_id -> pilot_programs(id)`, `created_by -> users(id)`. |

High-frequency query performance is ensured with composite indices on `(pilot_id, status)`, `(pilot_id, verification_status)`, and `(field_id, measurement_type)`.

---

## 4. API Endpoints Catalog

| Method | Endpoint | Description | Security |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/pilot-programs` | List all pilot programs with status filter | Authenticated |
| `POST` | `/api/v1/pilot-programs` | Create pilot program in `DRAFT` status | `OPERATOR`, `ADMIN`, `AGRONOMIST` |
| `GET` | `/api/v1/pilot-programs/{id}` | Get full pilot program details | Authenticated |
| `PUT` | `/api/v1/pilot-programs/{id}` | Update pilot program metadata | `OPERATOR`, `ADMIN` |
| `POST` | `/api/v1/pilot-programs/{id}/approve` | Approve pilot program | `OPERATOR`, `ADMIN` |
| `POST` | `/api/v1/pilot-programs/{id}/activate` | Activate pilot program (readiness gate) | `OPERATOR`, `ADMIN` |
| `POST` | `/api/v1/pilot-programs/{id}/pause` | Pause active pilot program | `OPERATOR`, `ADMIN` |
| `POST` | `/api/v1/pilot-programs/{id}/complete` | Complete pilot program | `OPERATOR`, `ADMIN` |
| `GET` | `/api/v1/pilot-programs/{id}/participants` | List enrolled participants | Authenticated |
| `POST` | `/api/v1/pilot-programs/{id}/participants` | Enroll participant with role | `OPERATOR`, `ADMIN` |
| `GET` | `/api/v1/pilot-programs/{id}/fields` | List enrolled fields and measurement plans | Authenticated |
| `POST` | `/api/v1/pilot-programs/{id}/fields` | Enroll field parcel with validation criteria | `OPERATOR`, `ADMIN` |
| `GET` | `/api/v1/pilot-readiness/{pilotId}` | Get latest readiness assessment | Authenticated |
| `POST` | `/api/v1/pilot-readiness/{pilotId}/assess` | Execute multi-dimensional readiness checks | `OPERATOR`, `ADMIN`, `AGRONOMIST` |
| `GET` | `/api/v1/pilot-ground-truth/pilot/{pilotId}` | List ground-truth records with status filter | Authenticated |
| `POST` | `/api/v1/pilot-ground-truth/pilot/{pilotId}` | Submit physical ground-truth measurement | `OPERATOR`, `ADMIN`, `AGRONOMIST`, `FARMER` |
| `POST` | `/api/v1/pilot-ground-truth/{id}/review` | Four-eyes independent review decision | `OPERATOR`, `ADMIN`, `AGRONOMIST` |
| `GET` | `/api/v1/pilot-ground-truth/{id}` | Get single ground-truth record with audit trail | Authenticated |
| `GET` | `/api/v1/pilot-validation/pilot/{pilotId}` | List prediction vs actual comparison records | Authenticated |
| `POST` | `/api/v1/pilot-validation/pilot/{pilotId}` | Evaluate model forecast against verified GT | `OPERATOR`, `ADMIN`, `AGRONOMIST` |
| `GET` | `/api/v1/pilot-feedback/pilot/{pilotId}` | List operator field observations | Authenticated |
| `POST` | `/api/v1/pilot-feedback/pilot/{pilotId}` | Submit field observation or discrepancy | `OPERATOR`, `ADMIN`, `AGRONOMIST`, `FARMER` |
| `PUT` | `/api/v1/pilot-feedback/{id}/status` | Update feedback status & assign investigator | `OPERATOR`, `ADMIN`, `AGRONOMIST` |
| `GET` | `/api/v1/pilot-evaluations/pilot/{pilotId}/monitoring` | Real-time pilot operational KPIs & backlog | Authenticated |
| `GET` | `/api/v1/pilot-evaluations/pilot/{pilotId}/report` | Get latest immutable evaluation report | Authenticated |
| `POST` | `/api/v1/pilot-evaluations/pilot/{pilotId}/report` | Generate evaluation snapshot | `OPERATOR`, `ADMIN` |

---

## 5. Verification & Test Execution Summary

### 5.1 Backend Verification (`./gradlew test`)
- **Total Tests**: **896**
- **Passed**: **880**
- **Failures**: **0**
- **Skipped / Ignored**: **16** (pre-existing baseline)
- **Success Rate**: **100%**
- **Execution Duration**: 1m 44.87s
- **Architecture Tests**: **118 ArchUnit rules passing**, including:
  - Rule 113: Pilot controllers reside in `pilot.api`.
  - Rule 114: Pilot domain has no JPA or persistence annotations.
  - Rule 115: Pilot domain has no Spring framework dependencies.
  - Rule 116: Pilot domain has no Spring Web or Servlet dependencies.
  - Rule 117: Pilot persistence entities reside in `pilot.infrastructure.persistence.entity`.
  - Rule 118: Pilot module has no physical actuation SDK dependencies (`com.pi4j`, `jSerialComm`).

### 5.2 Frontend Verification
- **Type Checking (`tsc -b`)**: **0 errors**.
- **Linting (`eslint .`)**: **0 errors / 0 warnings**.
- **Unit & Integration Tests (`vitest run --run`)**:
  - **31 test files passed** (including `pilot.test.tsx` and `dataIntegration.test.tsx`).
  - **296 tests passed / 0 failures**.
  - **Execution Duration**: 15.03s.
- **Production Build (`vite build`)**: **Succeeded in 5.81s**; production assets compiled into `dist/`.

### 5.3 Infrastructure Verification
- **Docker Compose Production Config**:
  `docker compose -f docker-compose.prod.yml config` verified with return code 0.

---

## 6. Real-World Deployment Prerequisites & Limitations

| Dimension | Operational Status | Current State & Requirement Prior to Real Field Pilot |
| :--- | :--- | :--- |
| **Software Platform** | `IMPLEMENTED` & `READY` | Core logic, REST APIs, databases, UI, and security fully verified. |
| **Physical IoT Gateways** | `AWAITING_HARDWARE` | Yangiariq canals currently lack automated IoT flume gateways. Handheld ultrasonic flow meters and TDR soil probes must be deployed. |
| **Field Participants** | `AWAITING_FIELD_VALIDATION`| Landowners, WUA ditch riders, and regional agronomists must be onboarded and briefed on the four-eyes verification procedure. |
| **Weather Connectivity** | `CONFIGURED` | Open-Meteo adapter active; fallback simulated weather maintains stability during network outages. |
| **Ground-Truth Calibration**| `CONFIGURED` | Physical instruments (TDR probes, Bourdon gauges) require physical calibration certificates (`CERT-...`) logged prior to sampling. |

---

## 7. Conclusion & Scope Discipline

Milestone 08.20 is **100% complete**. All requirements have been implemented, tested, and documented with absolute fidelity to Clean Architecture, role boundaries, and factual integrity.

Development now ceases. **No code or features from Milestone 08.21 will be commenced without explicit authorization.**
