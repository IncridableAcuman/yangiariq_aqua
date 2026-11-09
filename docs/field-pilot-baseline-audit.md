# Baseline Audit: Controlled Field Pilot & Operational Acceptance (Milestone 08.20)

## 1. Executive Summary
Before initiating the design and implementation of Milestone 08.20 (**Controlled Field Pilot, Ground-Truth Validation & Operational Acceptance**), this baseline audit inspects all pre-existing subsystems of **YANGIARIQ AQUA AI** (Milestones 08.01–08.19).

The objective is to establish an uncompromised inventory of existing reusable modules, identify missing pilot governance mechanisms, document existing system boundaries, assess external dependencies, and specify risks that must be resolved prior to any physical field pilot execution in Khorezm.

---

## 2. Baseline Verification Results

### 2.1 Backend Baseline (`./gradlew clean test`)
- **Total Tests**: **878**
- **Passed**: **862**
- **Failures**: **0**
- **Ignored / Skipped**: **16** (pre-existing baseline)
- **Architecture Rules**: **112 ArchUnit rules** enforced and passing (including Rules 107–112 for the `integration` module).
- **Execution Time**: 2m 2.85s (total Gradle build 2m 56s).

### 2.2 Frontend Baseline
- **Type Checking (`tsc -b`)**: 0 errors.
- **Linting (`eslint .`)**: 0 errors / 0 warnings.
- **Unit & Integration Tests (`vitest run --run`)**: **30 test files, 284 tests passed, 0 failures**.
- **Production Build (`vite build`)**: Succeeded in 5.68s.

---

## 3. Existing Reusable Capabilities (By Module)

| Module | Existing Capabilities Available for Reuse | How Pilot Framework Will Reuse It |
| :--- | :--- | :--- |
| **`field` / `user`** | Farms, parcels, boundary coordinates, soil types, crop classifications, owner mappings, RBAC roles (`ADMIN`, `OPERATOR`, `AGRONOMIST`, `FARMER`). | Reused directly. Pilot programs reference existing fields and users without creating duplicate ownership or parcel registries. |
| **`sensor`** | Sensor aggregate, `SensorReading`, `SensorProvider` SPI, `SimulatedSensorProvider`. | Reused directly. Pilot field sensors link to canonical sensor aggregates. |
| **`integration`** | `DataSource` registry, `CanonicalUnitConverter`, `IngestionPipelineService`, `IngestedProvenance`, `ExternalWeatherClient`, `HttpSensorGatewayProvider`, `ProcessCsvImportUseCase`. | Reused directly. Ingested telemetry flows through the existing canonical converter and provenance ledger. |
| **`prediction`** | Predictive models, model versions, horizons, prediction logs, MAE/RMSE evaluation, challenger models. | Extended for pilot-specific ground-truth outcome comparisons. |
| **`accountability`** | Incidents, investigations, root cause analyses, corrective action plans, SLAs, audit trails. | Reused directly. Discrepancies and pilot anomalies link to existing incident and corrective action entities. |
| **`performance`** | Regional operational KPIs, baseline comparability, impact assessments, conservation indicators. | Reused directly. Pilot outcome evaluation synthesizes verified pilot metrics with historical baseline methodology. |
| **`wateraccounting`** | Water mass balance, turnouts, conveyance loss calculations, ledger transactions, reconciliation. | Reused directly. Pilot water management outcomes reference audited ledger balances. |
| **`risk`** | Predictive risk intelligence, stress indices, drought vulnerability, canal network risk. | Reused directly. Pilot monitoring tracks operational and agronomic risk levels across pilot parcels. |

---

## 4. Missing Pilot-Specific Capabilities (Scope for Milestone 08.20)

1. **Pilot Program Governance**:
   - No formal aggregate exists to define pilot scope, boundaries, time envelopes, responsible organizations, program owners, or approval states (`DRAFT` $\rightarrow$ `ACTIVE` $\rightarrow$ `COMPLETED`).
2. **Pre-Flight Readiness Assessment**:
   - No automated multi-dimensional gate evaluating Infrastructure, Data, Operational, and Governance readiness before granting operational activation.
3. **Independent Ground-Truth Validation Workflow**:
   - While manual measurements exist (`com.aquaai.integration`), there is no workflow enforcing the **four-eyes principle** where an independent operator reviews, verifies, rejects, or flags a measurement for remeasurement against physical evidence.
4. **Prediction vs. Verified Actual Outcome Matching**:
   - Existing model evaluation evaluates historical batch logs; a pilot-focused comparison connecting specific field ground-truth records with active model horizons, version provenance, and statistical error metrics is missing.
5. **Structured Field Operator Feedback**:
   - No structured mechanism exists for field agronomists to log qualitative operational observations, sensor anomalies, weather discrepancies, or usability feedback linked to pilot entities.
6. **Defensible Pilot Outcome Evaluation**:
   - No evaluation snapshot mechanism exists to generate a defensible pilot report that clearly delineates directly observed results, calculated indicators, estimated outcomes, and explicit limitations.

---

## 5. Existing Limitations & Operational Boundaries

1. **No Live Hardware in the Field**:
   - Yangiariq canal turnouts and farm fields do not currently possess physical automated IoT telemetry gateways. The `HttpSensorGatewayProvider` is in status `AWAITING_HARDWARE`.
2. **Simulated Telemetry Remains SIMULATED**:
   - The platform generates synthetic edge sensor data for testing. The pilot framework must **never** treat simulated telemetry as verified ground truth.
3. **Four-Eyes Verification Constraint**:
   - A person submitting a ground-truth measurement cannot verify their own submission. Independent attestation by an authorized operator or agronomist is mandatory.
4. **No Autonomous Actuation**:
   - The system is an agricultural water intelligence and decision-support platform. It does not actuate pumps, valves, or canal gates.
5. **No Unsupported Agricultural Claims**:
   - Water savings, yield improvements, and financial benefits must not be claimed unless validated against verified baselines and statistically sufficient observations.

---

## 6. Required Infrastructure & External Dependencies

| Dependency | Status | Pilot Operational Impact |
| :--- | :--- | :--- |
| **PostgreSQL 16** | Ready & Tested | Core database storing pilot programs, readiness checks, validations, feedback, and snapshots. |
| **Liquibase Migrations** | Ready & Tested | Changeset `027-create-field-pilot-tables.sql` for schema changes. |
| **Spring Security / JWT** | Ready & Tested | Enforces strict role separation (`ADMIN`, `OPERATOR`, `AGRONOMIST`, `FARMER`) and prevents cross-farmer IDOR. |
| **Open-Meteo Weather API** | Configurable | Secondary provider; graceful fallback to simulated weather prevents pilot system crashes if network is offline. |
| **Physical Field Hardware** | Not Yet Deployed | Pilot framework must support manual measurements and CSV imports while awaiting hardware. |

---

## 7. Pilot Risks & Mitigation Strategy

1. **Risk of False Operational Validation**:
   - *Mitigation*: Multi-stage readiness gate where mandatory BLOCKED items prevent pilot activation. Explicit evidence classifications (`OBSERVED`, `VERIFIED`, `ESTIMATED`, `SIMULATED`).
2. **Risk of Single-Observer Bias**:
   - *Mitigation*: Four-eyes principle on ground-truth verification. Submitter and verifier must be distinct users.
3. **Risk of Premature Model Promotion**:
   - *Mitigation*: Prediction vs actual evaluation requires statistically sufficient sample sizes before any model recommendation.
4. **Risk of Actuation Misunderstanding**:
   - *Mitigation*: Strict architectural boundary prohibiting physical valve or pump control SDKs.
