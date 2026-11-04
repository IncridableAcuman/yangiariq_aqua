# Hybrid ML Prediction Engine Architecture

## 1. Architectural Philosophy
YANGIARIQ AQUA AI adopts a **safety-first hybrid prediction architecture** where deterministic agronomic principles remain authoritative and machine learning models are strictly bounded:

```text
Deterministic Agronomic Baseline (Physical Mass Balance & Crop Evapotranspiration)
                                 +
    Governed Machine Learning Candidate (L2-Regularized Residual Corrector)
                                 ↓
         Bounded Residual Adjustment: Max ±5.0% Volumetric Shift
                                 ↓
           Physical Constraints Clamp: Soil Moisture in [0.0%, 100.0%]
                                 ↓
     AUTHORITATIVE HYDRAULIC INTERLOCKS & SAFETY GATES (Absolute Dominance)
                                 ↓
                          Final Prediction
```

## 2. Non-Negotiable Core Principle
> **ML predicts. Deterministic agronomic and hydraulic rules constrain. Human governance decides production activation.**

Machine Learning candidates:
- **MUST NEVER** override hydraulic safety interlocks.
- **MUST NEVER** alter pump or valve states directly.
- **MUST NEVER** autonomously activate themselves or alter production configuration.
- **MUST NEVER** adjust safety thresholds or allocation quotas.
- Always fall back to the deterministic baseline in the event of missing features, drift, or telemetry degradation.

## 3. Mathematical Residual Formulation
For target prediction $y$ (such as soil moisture percentage at horizon $t + H$):

$$y_{\text{hybrid}} = \text{clamp}_{[0, 100]}\left(y_{\text{baseline}} + \text{clamp}_{[-5.0, +5.0]}(\hat{r}_{\text{ML}})\right)$$

Where:
- $y_{\text{baseline}}$ is computed via Penman-Monteith reference evapotranspiration, soil texture infiltration models, and crop coefficients.
- $\hat{r}_{\text{ML}} = \mathbf{w}^T \mathbf{x} + b$ is the residual learned by the Ridge regression candidate on standardized historical features $\mathbf{x}$.
- The residual correction $\hat{r}_{\text{ML}}$ is bounded strictly within $[-5.0\%, +5.0\%]$ to prevent runaway model drift or out-of-distribution hallucinations.

## 4. Fallback Guarantees
If any of the following conditions occur at inference time:
1. Candidate model is not in `ACTIVE`, `SHADOW`, or `CANARY` status.
2. Required features have missing or corrupt telemetry.
3. Hydraulic block is active for the field.
4. Telemetry data quality is flagged as `COMPROMISED` or `UNRELIABLE`.

The engine automatically silences the ML residual ($\hat{r}_{\text{ML}} = 0$) and returns the deterministic agronomic baseline.
