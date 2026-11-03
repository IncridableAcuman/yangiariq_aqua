# Adaptive Method Selection & 9-Level Hierarchy

## 1. Overview

The `AdaptiveSelectionEngine` is a pure, deterministic domain service residing in `com.aquaai.prediction.domain.service`. It evaluates governed prediction method versions against an incoming parcel's `PredictionContext` using a strict 9-level hierarchy.

Every decision produces an immutable audit record (`AdaptiveSelectionDecision`) persisted to `adaptive_selection_decisions` in the database, containing the context snapshot, candidate scores, and explainability reasoning.

---

## 2. Authoritative 9-Level Selection Hierarchy

```
LEVEL 1: Hydraulic Safety Priority Check (Active CRITICAL anomaly → Safety Dominant Baseline)
   ↓
LEVEL 2 & 3: Governance Status & Module Availability (Exclude ARCHIVED, DEPRECATED, or DRAFT)
   ↓
LEVEL 4: Physical Sensor Availability & Telemetry Quality Gate (MISSING sensors → Fallback)
   ↓
LEVEL 5: Agronomic Context Match Precedence (EXACT > PARTIAL > SIMILAR > BASELINE)
   ↓
LEVEL 6: Empirical Accuracy Ranking & Sample Size Gating (n ≥ 10 threshold strictly enforced)
   ↓
LEVEL 7: Confidence Reliability Calibration (Adjusted by telemetry health & slice health)
   ↓
LEVEL 8: Contextual Performance Drift & Local Health Check (DEGRADED candidates rejected)
   ↓
LEVEL 9: Safe Governed Baseline Fallback (Active baseline version deployed as safe default)
```

### Hierarchy Breakdown:

1. **Level 1: Hydraulic Safety Priority Check**:
   - If `context.recentAnomalySeverity() == AnomalySeverityContext.CRITICAL`:
     - Returns `SAFETY_INTERLOCK_DOMINANT`.
     - Stamped with active baseline configuration.
     - Operational execution remains blocked until hydraulic safety interlocks are cleared.

2. **Level 2 & 3: Governance Status Filter**:
   - Only versions in `ACTIVE`, `APPROVED`, or `SHADOW` status are eligible.
   - `DRAFT`, `RETIRED`, `DEPRECATED`, and unreviewed candidate versions are strictly rejected.

3. **Level 4: Physical Sensor Availability Gate**:
   - For `SOIL_MOISTURE_FORECAST`, if `sensorAvailability == MISSING`:
     - Returns `INSUFFICIENT_DATA` mode with 20% confidence.
     - Prevents misleading precision without physical ground-truth sensors.

4. **Level 5: Agronomic Context Match Precedence**:
   - Evaluates empirical context match levels:
     - `EXACT_CONTEXT`: Matches Crop + Soil + Irrigation Method + Telemetry Quality.
     - `PARTIAL_CONTEXT`: Matches Crop + Irrigation Method.
     - `SIMILAR_CONTEXT`: Matches Crop type only.
     - `BASELINE_FALLBACK`: Falls back to global version profile.

5. **Level 6: Empirical Accuracy Ranking & Sample Gating ($n \ge 10$)**:
   - A slice is **only eligible** if historical evaluations $n \ge 10$.
   - Slices with $n < 10$ are marked gated (`hasSufficientSample = false`) and cannot trigger context-specific promotion over baseline.
   - Ranking formula:
     $$\text{Score} = \text{BaseMatch} + \max(0, 40 - \text{MAE} \times 1.5) + \min(10, n / 5) + \text{StatusBonus}$$

6. **Level 7: Confidence Reliability Calibration**:
   - Base confidence is scaled by match level:
     - `EXACT_CONTEXT`: 88%
     - `PARTIAL_CONTEXT`: 80%
     - `SIMILAR_CONTEXT`: 72%
     - `BASELINE_FALLBACK`: 65%
   - Modifiers: Telemetry `HIGH` (+5%), `LOW` (-10%), `INSUFFICIENT` (-20%), Context Health `WATCH` (-8%).

7. **Level 8: Contextual Performance Drift & Local Health Check**:
   - If candidate profile has `contextualModelHealth == DEGRADED` (detected by `ContextualDriftDetector`), it is rejected.

8. **Level 9: Safe Governed Baseline Fallback**:
   - If no candidate version surpasses statistical significance or context requirements, the active governed baseline is safely deployed.

---

## 3. Explainability & Audit Trail

Each selection decision is saved with:
- `prediction_id`: Nullable UUID linking to forecast record.
- `field_id`: Target field UUID.
- `prediction_type`: Forecast domain.
- `selected_version_tag`: E.g., `SOIL_MOISTURE_V2`.
- `selection_mode`: `EXACT_CONTEXT`, `PARTIAL_CONTEXT`, etc.
- `selection_reason`: Human-readable natural language summary.
- `evidence_summary`: Quantified evidence (sample count, MAE, bias).
- `context_snapshot_json`: Full serialized `PredictionContext`.
- `alternatives_json`: Array of evaluated candidates with rejection reasons.
- `confidence_percent`: Calibrated confidence rating.
- `created_at`: UTC timestamp.
