# Milestone 08.3 Implementation Plan: Telemetry Intelligence, Anomaly Detection & Agronomic Recommendations

## Executive Summary
Milestone 08.3 establishes the unified telemetry intelligence, deterministic anomaly detection, and agronomic recommendation layer for Yangiariq Aqua AI.
The system transforms raw physical telemetry into normalized signals, detects trends and anomalies, performs deterministic agronomic assessments, and produces explainable recommendations strictly for human decision-support.

Physical actuation (controlling valves, pumps, or actuators) is **strictly prohibited**. The authoritative safety hierarchy ($\text{SAFETY} \to \text{QUOTA} \to \text{INFRASTRUCTURE} \to \text{AGRONOMIC} \to \text{WEATHER} \to \text{PRIORITY} \to \text{AI}$) is preserved without compromise.

---

## 1. Architectural Chain
```text
SensorReading
      ↓
Telemetry Normalization (TelemetrySignal + Freshness)
      ↓
Temporal Trend Analysis (Δmoisture / Δtime)
      ↓
Deterministic Anomaly Detection (Rule Engine)
      ↓
Agronomic Assessment (AgronomicAssessmentEngine)
      ↓
Hydraulic Safety & Water Budget Interlock Check
      ↓
AI Advisory Explanation (Optional & Non-authoritative)
      ↓
Irrigation Recommendation (Decision Support)
      ↓
Human Review & Approval
      ↓
Smart Irrigation Plan (Formal Scheduling)
```

---

## 2. Reusable Existing Components
- **`com.aquaai.sensor`**:
  - `SensorReading` aggregate remains the canonical physical observation.
  - `ReadingQuality` (`VALID`, `SUSPECT`, `INVALID`) guarantees data integrity semantics.
  - `SensorReadingRepository` with optimized indexed queries (`findLatestByFieldIdGroupedByType`, `findByFieldId`).
- **`com.aquaai.anomaly`**:
  - `Anomaly` aggregate, rule configurations, deduplication indexes, and lifecycle (`DETECTED`, `ACKNOWLEDGED`, `INVESTIGATING`, `RESOLVED`, `DISMISSED`).
  - Rule detectors for soil moisture, flow, pressure, temperature, salinity, humidity, and sensor health.
- **`com.aquaai.recommendation`**:
  - `AgronomicRecommendation` aggregate, lifecycle, and corroborating evidence structures.
  - `CompositeAgronomicAIProvider`, `GeminiAgronomicAIAdapter`, and `MockAgronomicAIAdapter` for advisory explanations.
- **`com.aquaai.irrigation`**:
  - `HydraulicSafetyInterlock` enforcing physical safety boundaries (pipe burst, pressure collapse).
  - `WaterBudget` tracking allocated, consumed, committed, and remaining water volume.
- **`com.aquaai.field`**:
  - `FieldOwnershipChecker` ensuring strict Anti-IDOR enforcement (HTTP 404 for unauthorized parcel queries).

---

## 3. Bounded Context Architecture: `com.aquaai.intelligence`
A clean-architecture package structure:
```text
com.aquaai.intelligence
├── domain
│   ├── model
│   │   ├── TelemetrySignal.java
│   │   ├── TelemetryFreshness.java (FRESH, STALE, MISSING)
│   │   ├── TelemetryTrend.java (INCREASING, DECREASING, STABLE, rateOfChangePerHour)
│   │   ├── TrendDirection.java
│   │   ├── AgronomicSignalType.java (SOIL_TOO_DRY, SOIL_TOO_WET, HIGH_TEMPERATURE, etc.)
│   │   ├── IntelligenceSeverity.java (INFO, LOW, MEDIUM, HIGH, CRITICAL)
│   │   ├── IntelligenceConfidence.java (HIGH, MEDIUM, LOW)
│   │   ├── AgronomicSignal.java
│   │   ├── AgronomicAssessment.java (Aggregate Root)
│   │   └── IntelligenceRecommendation.java
│   ├── service
│   │   ├── TelemetryNormalizationService.java
│   │   ├── TemporalTrendCalculator.java
│   │   ├── FreshnessEvaluator.java
│   │   ├── AgronomicAssessmentEngine.java
│   │   └── IntelligenceRecommendationEngine.java
│   └── port
│       ├── IntelligenceFieldPort.java
│       ├── IntelligenceTelemetryPort.java
│       ├── IntelligenceAnomalyPort.java
│       ├── IntelligenceWaterBudgetPort.java
│       ├── IntelligenceHydraulicSafetyPort.java
│       ├── IntelligenceAIExplanationPort.java
│       └── AgronomicAssessmentRepository.java
├── application
│   ├── usecase
│   │   ├── EvaluateFieldIntelligenceUseCase.java
│   │   ├── GetFieldAgronomicAssessmentUseCase.java
│   │   ├── ListAgronomicAssessmentsUseCase.java
│   │   └── GetFieldTelemetrySignalsUseCase.java
│   └── dto
│       ├── AgronomicAssessmentResponse.java
│       ├── AgronomicSignalResponse.java
│       └── TelemetrySignalResponse.java
├── infrastructure
│   ├── adapter
│   │   ├── IntelligenceFieldAdapter.java
│   │   ├── IntelligenceTelemetryAdapter.java
│   │   ├── IntelligenceAnomalyAdapter.java
│   │   ├── IntelligenceWaterBudgetAdapter.java
│   │   ├── IntelligenceHydraulicSafetyAdapter.java
│   │   └── IntelligenceAIExplanationAdapter.java
│   └── persistence
│       ├── AgronomicAssessmentJpaEntity.java
│       ├── AgronomicAssessmentSignalJpaEntity.java
│       ├── SpringDataAgronomicAssessmentRepository.java
│       ├── AgronomicAssessmentRepositoryImpl.java
│       └── AgronomicAssessmentJpaMapper.java
└── api
    └── IntelligenceController.java
```

---

## 4. Telemetry Normalization, Quality & Freshness Invariants
1. **Canonical Source**: `SensorReading` is never modified or duplicated; it is mapped into normalized `TelemetrySignal`s for intelligence processing.
2. **Quality Invariants**:
   - `INVALID` readings are disqualified from driving agronomic recommendations.
   - `SUSPECT` readings degrade overall assessment confidence to `LOW` or `MEDIUM` and trigger a warning signal.
   - `VALID` readings drive deterministic thresholds.
3. **Freshness Policies**:
   - `SOIL_MOISTURE`: Fresh $\le 6\text{h}$; Stale $6-24\text{h}$; Missing $> 24\text{h}$ or absent.
   - `TEMPERATURE` / `HUMIDITY`: Fresh $\le 2\text{h}$; Stale $2-12\text{h}$; Missing $> 12\text{h}$ or absent.
   - `PRESSURE` / `WATER_FLOW`: Fresh $\le 30\text{min}$; Stale $30-120\text{min}$; Missing $> 120\text{min}$ or absent.
   - Stale telemetry forces confidence penalty; missing critical telemetry emits `NO_RECENT_TELEMETRY` signal.

---

## 5. Temporal Trend Analysis
- Evaluates recent telemetry points (e.g., last 3 to 12 hours).
- Calculates:
  $$\Delta \text{value} / \Delta t = \frac{v_t - v_0}{t - t_0} \quad (\text{expressed as \% per hour})$$
- Directions:
  - `DECREASING`: rate $< -0.20\%/\text{h}$
  - `INCREASING`: rate $> +0.20\%/\text{h}$
  - `STABLE`: $|rate| \le 0.20\%/\text{h}$
- Utilizes `BigDecimal` with rounding mode `HALF_UP` to prevent floating-point anomalies.

---

## 6. Agronomic Assessment & Deterministic Rules
Evaluates signals combining telemetry, field soil/crop context, active anomalies, and weather:
- **`SOIL_TOO_DRY`**:
  - Moisture $< 15\% \implies$ `CRITICAL`
  - Moisture $15\% - 20\% \implies$ `HIGH`
- **`SOIL_TOO_WET`**: Moisture $> 35\% \implies$ `MEDIUM`
- **`HIGH_TEMPERATURE`**: Ambient $> 40^\circ\text{C} \implies$ `HIGH`
- **`LOW_TEMPERATURE`**: Ambient $< 2^\circ\text{C} \implies$ `HIGH` (frost hazard)
- **`LOW_HUMIDITY`**: Relative humidity $< 18\% \implies$ `MEDIUM` (dry garmsel)
- **`PRESSURE_DROP` / `ABNORMAL_FLOW`**:
  - Mainline pressure $< 1.2\text{ bar}$ or pipe burst anomaly active $\implies$ `CRITICAL`, triggers hydraulic safety block.
- **`NO_RECENT_TELEMETRY`**: Critical sensors missing or offline $\implies$ `HIGH`.

---

## 7. Water Budget & Safety Interlock Integration
- Integrates with `HydraulicSafetyInterlock`:
  - If pipe burst or catastrophic pressure drop is detected, `hydraulicSafetyBlocked = true`.
  - The system emits `IRRIGATION_BLOCKED` advisory; AI or heuristics **CANNOT** recommend watering.
- Integrates with `WaterBudget`:
  - If remaining allocation $\le 0\text{ m}^3$, `waterBudgetConstrained = true`.
  - Recommendation explicitly warns of quota exhaustion (`WATER_BUDGET_CONSTRAINT`).

---

## 8. AI Advisory Role & Fallback
- Deterministic calculation runs first and produces authoritative values (volume, duration, thresholds).
- The `IntelligenceAIExplanationPort` generates advisory narrative context (risk explanation, local Yangiariq agronomical insights).
- If Gemini / external LLM fails, timeout occurs, or is offline, fallback provides comprehensive deterministic explanations without failing the request.

---

## 9. Security & Anti-IDOR Model
- Strict tenant authorization via `FieldOwnershipChecker`.
- **FARMER**:
  - Accessing foreign parcel $\implies$ HTTP 404 (anti-enumeration).
  - Mutating foreign parcel $\implies$ HTTP 404.
- **OPERATOR**:
  - Read-only district monitoring.
- **ADMIN**:
  - District-wide access.

---

## 10. Database Strategy
- Liquibase migration `013-create-intelligence-domain-tables.sql`:
  - Table `agronomic_assessments`
  - Table `agronomic_assessment_signals`
  - Bounded indexes on `(field_id, assessed_at DESC)` and `(assessment_id)`.
- Reuses existing `anomalies` and `recommendations` tables.

---

## 11. REST API Design
- `POST /api/v1/fields/{fieldId}/intelligence/evaluate` (evaluates latest telemetry & persists assessment)
- `GET /api/v1/fields/{fieldId}/intelligence/assessment` (retrieves latest assessment)
- `GET /api/v1/fields/{fieldId}/intelligence/signals` (retrieves normalized telemetry signals & freshness)
- `GET /api/v1/fields/{fieldId}/intelligence/history` (paginated assessment history)
- `POST /api/v1/fields/{fieldId}/recommendations/evaluate` (recommendation evaluation alias)

---

## 12. ArchUnit Architecture Rules
Adds 14 mandatory rules in `ArchitectureTests.java`:
1. `intelligence.domain` does not depend on Spring
2. `intelligence.domain` does not depend on JPA
3. `intelligence.domain` does not depend on PostgreSQL
4. `intelligence.domain` does not depend on Gemini
5. `intelligence.domain` does not depend on HTTP
6. `intelligence.domain` does not depend on sensor infrastructure
7. `intelligence.application` does not depend on controllers
8. API does not access repositories directly
9. API does not expose JPA entities
10. AI provider implementations remain infrastructure adapters
11. Deterministic calculation remains independent of AI
12. Intelligence does not create circular dependency with irrigation
13. Intelligence does not directly control hardware
14. Intelligence does not bypass `HydraulicSafetyInterlock`
All existing 37 rules remain strictly preserved.

---

## 13. Frontend Integration
- Types & API client in `frontend/src/entities/intelligence/`
- Agronomic assessment widget displaying:
  - Observation $\to$ Anomaly $\to$ Recommendation $\to$ Decision flow
  - Telemetry freshness indicators (`FRESH`, `STALE`, `MISSING`)
  - Soil moisture trend icons ($\nearrow, \searrow, \to$)
  - Hydraulic safety block indicators
  - Water budget constraint alerts
- Fully localized in UZ, RU, EN.

---

## 14. Verification Strategy
1. **Unit Tests**:
   - `TelemetrySignalTest` (validation, unit compatibility, physical bounds)
   - `TemporalTrendCalculatorTest` (trend math, insufficient history, irregular intervals)
   - `FreshnessEvaluatorTest` (fresh, stale, missing SLA windows)
   - `AgronomicAssessmentEngineTest` (soil moisture, temperature, humidity, safety blocks)
   - `IntelligenceRecommendationEngineTest` (budget constraints, safety blocking, priority ranking)
2. **Integration Tests**:
   - `IntelligenceSecurityIntegrationTest` (Farmer foreign field $\implies$ 404, own field $\implies$ 200)
   - `IntelligenceEvaluationIntegrationTest` (full pipeline execution)
   - `IntelligencePersistenceIntegrationTest` (database round-trip)
3. **Architecture Tests**:
   - All 51 ArchUnit rules pass without warnings.
4. **Frontend Tests**:
   - Vitest test suite for intelligence widgets, freshness tags, and trend rendering.
   - `npm run typecheck`, `npm run lint`, `npm run build`.
