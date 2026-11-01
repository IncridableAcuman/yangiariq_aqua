# Agronomic Recommendations & Decision Support (`docs/agronomic-recommendations.md`)

## 1. Distinction: Recommendation vs. Smart Irrigation Plan

A fundamental architectural boundary in Yangiariq Aqua AI is the distinction between advisory proposals and binding operational schedules:

```text
Telemetry & Anomalies
          ↓
Agronomic Recommendation ("What the system suggests")
          ↓
Human Review & Approval ("Farmer decision")
          ↓
Smart Irrigation Plan ("What has been formally committed")
          ↓
Operational Schedule & Monitoring
```

1. **Agronomic Recommendation (`AgronomicRecommendation` / `IntelligenceRecommendation`)**:
   - Represents an advisory hypothesis based on current soil conditions, weather, and anomalies.
   - Non-authoritative: AI or heuristic advice cannot automatically open valves, activate pumps, or commit water allocations.
   - Transparently presents confidence, constraints, evidence, and risk trade-offs.
2. **Smart Irrigation Plan (`SmartIrrigationPlan`)**:
   - The formal operational plan created by human intervention or approved from recommendations.
   - Binds volumetric quota against the parcel's `WaterBudget`.
   - Enforces strict immutable lifecycle transitions (`DRAFT` $\to$ `PROPOSED` $\to$ `APPROVED` $\to$ `SCHEDULED` $\to$ `IN_PROGRESS` $\to$ `COMPLETED`).

---

## 2. Recommendation Domain Model & Lifecycle

The aggregate root `com.aquaai.recommendation.domain.model.AgronomicRecommendation` maintains explicit lifecycle states:

```text
GENERATED ──> VIEWED ──> ACCEPTED ──> COMPLETED
    │            │           │
    ├───> DISMISSED <────────┘
    │          (reason required)
    └───> EXPIRED
```

- **Invariants**:
  - `DISMISSED`, `COMPLETED`, and `EXPIRED` are immutable terminal states.
  - Dismissal requires a non-blank audit explanation (`dismissalReason`).
  - Active recommendations for the same field and type are deduplicated via PostgreSQL partial unique index.

---

## 3. Corroborating Evidence & Actionable Steps

Each recommendation contains:
- **`RecommendationEvidence`**: Observed telemetry, metric threshold, unit, baseline, and human-readable explanation.
- **`RecommendedAction`**: Sequenced action items with urgency (`IMMEDIATE`, `WITHIN_24H`, `ROUTINE`) and explicit negative actions (`isNegativeAction = true`) to prevent hazardous mistakes (e.g., *"Do not irrigate during 11:00-17:00 heat wave"*).

---

## 4. Role-Based Access Control & Anti-IDOR Security

- **FARMER**: Own parcels only. Foreign parcel ID queries return `HTTP 404 Not Found` (anti-enumeration).
- **OPERATOR**: Read-only monitoring across the district.
- **ADMIN**: District-wide operational oversight.
