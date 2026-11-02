# Prediction Data Quality, Gating & Performance Drift (Milestone 08.8)

## 1. Overview & Objective

Predictive accuracy in agricultural IoT systems is inextricably linked to the quality and timeliness of telemetry feeds. Models cannot produce valid forecasts if telemetry gateways drop packets, sensor battery voltage drops, or soil moisture probes experience calibration drift.

Milestone 08.8 introduces rigorous data quality evaluation, sample-size gating, and automated performance drift detection.

---

## 2. Telemetry Data Quality Categories (`EvaluationDataQuality`)

Every evaluated prediction record records the telemetry quality present during the forecast and evaluation window:

| Quality Category | Telemetry Characteristics | Evaluation Impact |
|---|---|---|
| **`DATA_COMPLETE`** | Continuous telemetry from all configured probe depths ($20\,\text{cm}, 40\,\text{cm}, 60\,\text{cm}$) and local meteorological stations within authoritative $\le 60$-minute intervals. | Full numerical error calculation; highest evaluation confidence. |
| **`DATA_PARTIAL`** | Missing intermediate depth readings or intermittent packet dropouts ($10-30\%$), requiring spatial or temporal interpolation. | Evaluated with wider uncertainty bands; flagged in evaluation audit. |
| **`DATA_INSUFFICIENT`** | Sensor offline, battery dead, or telemetry stale ($> 24\,\text{hours}$). | Gated from regression metrics; assigned status `INSUFFICIENT_DATA` with failure reason `MISSING_SENSOR` or `STALE_TELEMETRY`. |

---

## 3. Sample-Size Gating & Thresholds

> [!IMPORTANT]
> To prevent false confidence, evaluation metrics are strictly gated based on empirical sample size ($n$).

- **Global & Type-Level Gating**:
  - Minimum sample size: **$n = 10$**.
  - If $n < 10$, aggregate numerical metrics ($\text{MAE}, \text{RMSE}, \text{MBE}$) and classification rates ($\text{Accuracy}, \text{Precision}, \text{Recall}, F_1$) are suppressed.
  - System displays: *"Insufficient data for evaluation ($n = X$, minimum required: 10). Performance metrics are honestly gated to prevent false confidence."*
- **Parcel-Level Gating**:
  - Minimum sample size: **$n = 5$**.
  - If $n < 5$, the parcel row displays the badge `limited sample (n < 5)` instead of misleading percentages.

---

## 4. Model Health Status Determination (`ModelHealthStatus`)

The domain service `PredictionEvaluationAggregator` calculates the operational health of each predictive model:

```
                               ┌─────────────────────────────┐
                               │  Sample Size Check: n < 10? │
                               └──────────────┬──────────────┘
                                      YES     │      NO
                                              ▼
                             ┌───────────────────────────────┐
                             │ Status = INSUFFICIENT_DATA    │
                             └───────────────────────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      │                                               │
                      ▼                                               ▼
      ┌───────────────────────────────┐               ┌───────────────────────────────┐
      │      Numerical Regression     │               │   Categorical Classification  │
      └───────────────┬───────────────┘               └───────────────┬───────────────┘
                      │                                               │
      • MAE > 6.0% or Bias > 8.0?                     • Accuracy < 70% or F1 < 0.65?
        → DEGRADED                                      → DEGRADED
      • MAE > 4.0% or Bias > 4.0?                     • Accuracy < 80% or F1 < 0.75?
        → WATCH                                         → WATCH
      • Otherwise:                                    • Otherwise:
        → HEALTHY                                       → HEALTHY
```

---

## 5. Automated Performance Drift Detection

Performance drift occurs when environmental conditions shift (e.g. onset of seasonal Khorezm summer heat waves, high canal salinity) or soil compaction alters water infiltration rates without model parameter updates.

### 5.1 Drift Detection Algorithm
1. Partitions evaluated predictions into:
   - **Recent Window ($W_{\text{recent}}$)**: Evaluated within the last 7 days.
   - **Baseline Historical Window ($W_{\text{baseline}}$)**: Evaluated between 8 and 30 days prior.
2. If both windows have sufficient samples ($n_{\text{recent}} \ge 5$, $n_{\text{baseline}} \ge 5$):
   - Computes $\text{MAE}_{\text{recent}}$ and $\text{MAE}_{\text{baseline}}$.
   - Computes the drift ratio:
     $$\Delta_{\text{drift}} = \frac{\text{MAE}_{\text{recent}} - \text{MAE}_{\text{baseline}}}{\text{MAE}_{\text{baseline}}}$$
3. **Drift Warning Trigger**:
   - If $\Delta_{\text{drift}} \ge 0.30$ ($+30\%$ degradation in accuracy):
     - Sets `performanceDriftDetected = true`
     - Sets `overallModelHealth = DEGRADED`
     - Publishes diagnostic alert: *"Performance Drift Detected: recent error variance exceeds baseline historical threshold by $\Delta_{\text{drift}}\%$."*
