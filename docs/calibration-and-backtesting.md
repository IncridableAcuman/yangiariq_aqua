# Historical Backtesting & Calibration Engine

## 1. Concept & Purpose

The backtesting engine (`BacktestingEngine.java`) deterministically simulates candidate prediction configurations against historical ground truth before any change can be presented to human administrators for activation.

```
+----------------------------------------------------------------------------------+
|                              Historical Ground Truth                             |
|          (Actual soil moisture telemetry, real water meter consumptions)          |
+----------------------------------------------------------------------------------+
                             /                         \
                            /                           \
                           v                             v
           +-------------------------------+   +-------------------------------+
           |    Current Baseline Version   |   |   Proposed Candidate Version  |
           |    (e.g., SOIL_MOISTURE_V1)   |   |    (e.g., SOIL_MOISTURE_V2)   |
           +-------------------------------+   +-------------------------------+
                           \                             /
                            \                           /
                             v                         v
                       +-------------------------------------+
                       |   Metric Evaluation & Regression    |
                       |                Guard                |
                       +-------------------------------------+
                                          |
                                          v
                               +---------------------+
                               | Candidate Comparison|
                               |  (Delta Analysis)   |
                               +---------------------+
```

---

## 2. Anti-Leakage Backtest Strategy

To prevent overfitting, data contamination, and optimistic bias, the backtesting engine adheres strictly to the following five rules:

1. **Temporal Horizon Bounding**: Backtests only evaluate closed operational windows in the past ($t < t_{\text{now}} - \text{horizon}$).
2. **Feature Availability at Prediction Time**: Features used for simulating candidate predictions are strictly bounded by timestamps $t \le t_{\text{prediction}}$. No sensor readings, irrigation logs, or weather observations recorded after $t_{\text{prediction}}$ are accessible to candidate formulas.
3. **Identical Sample Pairwise Matching**: Current and candidate predictions must evaluate against the exact same ground truth observations.
4. **Preserved Operational Semantics**: Units, error definitions, and tolerance thresholds are identical between versions.
5. **Deterministic Outcome Replay**: Running the backtest twice over the same date range yields identical metric deltas.

---

## 3. Evaluated Metrics & Primary Criteria

Depending on prediction type, the engine reuses authoritative domain evaluation metrics:

- **Regression Models** (`SOIL_MOISTURE_FORECAST`, `WATER_DEMAND_FORECAST`):
  - **MAE** (Mean Absolute Error): Primary calibration objective.
  - **RMSE** (Root Mean Squared Error): Sensitive to outlier spikes.
  - **MBE** (Mean Bias Error): Primary indicator of systematic over/under-prediction drift.
  - **Interval Hit Rate**: Percentage of observations within $[ \text{lowerBound}, \text{upperBound} ]$.
- **Classification Models** (`IRRIGATION_NEED_FORECAST`, `SENSOR_RELIABILITY_FORECAST`):
  - **Accuracy %**, **Precision**, **Recall**, **F1 Score**.
  - **Confusion Matrix**: True Positives, True Negatives, False Positives, False Negatives.

---

## 4. Multi-Factor Regression Guard (`RegressionGuard.java`)

A candidate version must never be approved based solely on a single metric improvement:

> **Example**: If candidate $V_2$ improves MAE from 8.2% to 5.9% (+28% improvement), but False Positives surge from 4% to 15% (triggering unnecessary operational interventions), the candidate is flagged.

### Regression Detection Rules
1. **RMSE Increase**: If MAE improves but RMSE increases by more than 5%, large prediction errors are worsening.
2. **Extreme Bias Shift**: If Mean Bias Error inverts from $+2\%$ to $-8\%$, the model has overcorrected into chronic underestimation.
3. **Precision/Recall Collapse**: If classification F1 degrades by more than 0.05, the candidate is flagged as `REGRESSION_DETECTED`.

When `regressionDetected = true`, the system records detailed regression diagnosis text in the backtest run and forces the proposal status to `REGRESSION_DETECTED`. Administrative approval requires explicit acknowledgment of the regression alert.
