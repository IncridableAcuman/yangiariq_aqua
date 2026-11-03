# Context-Aware Adaptive Prediction Engine

## 1. Architectural Philosophy

In traditional precision agriculture software, predictive algorithms either apply static hardcoded parameters across all fields, or attempt autonomous, black-box machine learning self-tuning that risks erratic or catastrophic operational recommendations (such as altering hydraulic safety pressures or crop water coefficients during unobserved sensor faults).

**YANGIARIQ AQUA AI** rejects both extremes. Milestone 08.10 establishes the **Context-Aware Adaptive Intelligence Engine**, guided by a foundational product principle:

> **"YANGIARIQ AQUA AI selects from human-governed prediction methods using verified historical performance for the current context."**

### Core Tenet: Governed Selection vs. Autonomous Self-Modification
- **ZERO Autonomous Self-Modification**: The platform never rewrites formulas, trains online neural weights, or automatically modifies Kc, ET0, or safety parameters in production.
- **Human Governance is Authoritative**: Every candidate method version must be registered by a human, simulated via historical backtesting, vetted through regression guards, approved by an administrator (`ROLE_ADMIN`), and deployed under controlled canary or active status.
- **Deterministic Selection**: For every parcel and forecast window, the system evaluates the real-time operational context against governed method versions and historical performance matrices, choosing the optimal method deterministically.

---

## 2. The Continuous Contextual Intelligence Loop

```
           OPERATIONAL PARCEL CONTEXT
           (Crop, Soil, Drip/Furrow, Telemetry Quality, Season, Weather, Headroom)
                               ↓
                   GOVERNED CANDIDATE METHODS
                   (Active, Approved, Shadow Versions)
                               ↓
                HISTORICAL PERFORMANCE MATRIX
                (Contextual MAE, RMSE, MBE, n ≥ 10 Gating)
                               ↓
                     CONTEXT MATCH PRECEDENCE
                     (Exact → Partial → Similar → Baseline)
                               ↓
                   DETERMINISTIC METHOD SELECTION
                   (Stamped with Version Tag & Config Snapshot)
                               ↓
                        FORECAST ISSUED
                               ↓
                         REAL OUTCOME
                         (Matched via 08.8 Telemetry Windows)
                               ↓
                     ACCURACY EVALUATION
                               ↓
                 CONTINUOUS LEARNING SIGNAL
                 (Updates Empirical Performance Slice)
```

---

## 3. Operational Context Dimensions

The context normalizer (`ContextNormalizer.java`) deterministically transforms disparate sensor readings, GIS metadata, and water accounts into standardized categorical and numerical dimensions:

| Dimension | Description | Valid States / Types |
| :--- | :--- | :--- |
| **Field Agronomics** | Crop type, soil texture, irrigation delivery technique, area | `COTTON`, `WHEAT`, `VINEYARD`, `MAIZE` / `SANDY_LOAM`, `LOAM`, `CLAY` / `DRIP`, `FURROW`, `SPRINKLER` |
| **Agricultural Season** | Continental Khorezm agricultural period | `SPRING`, `SUMMER`, `AUTUMN`, `WINTER` |
| **Telemetry Quality** | Valid reading ratio over the prior 72 hours | `HIGH` (≥85%), `MEDIUM` (60–84%), `LOW` (30–59%), `INSUFFICIENT` (<30%) |
| **Sensor Availability** | Physical moisture, flow, and weather sensor health | `COMPLETE`, `PARTIAL`, `MISSING`, `UNKNOWN` |
| **Water Headroom** | Remaining quota vs seasonal allocation | `ABUNDANT` (>50%), `NORMAL` (21–50%), `CONSTRAINED` (6–20%), `CRITICAL` (≤5%) |
| **Hydraulic Anomalies** | Active pipeline, pressure, or cavitation issues | `NONE`, `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| **Weather Environment** | Arid summer temperatures and forecast rain | `VERY_HOT` (≥35°C), `HOT`, `MODERATE`, `COOL` / `DRY`, `RAIN_EXPECTED` |

---

## 4. Hard Safety Dominance Guarantee

Under no circumstances can an adaptive model override hydraulic safety constraints:

1. **Hydraulic Interlock Precedence**: If an active `CRITICAL` hydraulic anomaly (such as delivery line pressure collapse or burst detection) exists on a parcel, the selection engine bypasses all statistical optimization and forces `SAFETY_INTERLOCK_DOMINANT`.
2. **Telemetry Failure Precedence**: If required physical soil sensors are `MISSING`, soil moisture forecasting falls back to `INSUFFICIENT_DATA` mode with degraded confidence (20%), preventing unwarranted operational confidence.
3. **Immutable Safety Constants**: Pipe burst pressure (`pipeBurstPressureBar`), emergency shutoff thresholds, and valve interlocks are hardcoded in domain models and rejected by parser regression guards if any configuration proposal attempts to alter them.
