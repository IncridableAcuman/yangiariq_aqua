# Contextual Empirical Performance & Drift Detection

## 1. Context Performance Profiling

A **Context Performance Profile** (`ContextPerformanceProfile.java`) represents observed empirical historical accuracy of a governed method version under a specific operational slice:

$$\text{Context Slice} = (\text{PredictionType}, \text{VersionTag}, \text{CropType}, \text{SoilType}, \text{IrrigationMethod}, \text{Season})$$

### Core Empirical Metrics:
- **Mean Absolute Error (MAE)**:
  $$\text{MAE} = \frac{1}{n} \sum_{i=1}^n |y_i - \hat{y}_i|$$
- **Root Mean Squared Error (RMSE)**:
  $$\text{RMSE} = \sqrt{\frac{1}{n} \sum_{i=1}^n (y_i - \hat{y}_i)^2}$$
- **Mean Bias Error (MBE)**:
  $$\text{MBE} = \frac{1}{n} \sum_{i=1}^n (y_i - \hat{y}_i)$$
  *(Captures persistent directional under-prediction or over-prediction).*
- **Sample Size ($n$)**:
  Total evaluated outcomes within the past 180 days.

---

## 2. Minimum Sample Size Gating ($n \ge 10$)

To prevent erratic parameter swings based on small-sample noise, the engine enforces strict statistical gating:

```
IF sample_size < 10 THEN
    hasSufficientSample = false
    contextualModelHealth = INSUFFICIENT_DATA
    contextDriftDetected = false
    Candidate CANNOT be promoted for context-tailored execution
ELSE
    hasSufficientSample = true
    Proceed to contextual error and drift evaluations
END IF
```

---

## 3. Contextual Drift Detection

While a predictive model may appear healthy when evaluated across an entire district, it may suffer severe localized performance degradation on a particular crop or irrigation method (e.g., severe under-prediction on clay soil under furrow irrigation during peak summer).

The `ContextualDriftDetector` compares the contextual slice against the version's global baseline:

1. **Error Degradation Threshold ($\ge 30\%$)**:
   $$\frac{\text{MAE}_{\text{context}}}{\text{MAE}_{\text{global}}} \ge 1.30$$
   Triggers a contextual drift warning (`isDriftDetected = true`) and marks contextual health as `DEGRADED`.

2. **Directional Bias Shift Threshold ($\ge 8\%$ swing)**:
   $$|\text{MBE}_{\text{context}} - \text{MBE}_{\text{global}}| \ge 8.0\%$$
   Identifies systematic over- or under-irrigation bias in specific microclimates.

When contextual drift is detected:
- The affected candidate version is flagged and rejected for that specific slice.
- An alert is raised in the Farm Intelligence Dashboard Action Center (`CONTEXTUAL_DRIFT_ALERT`).
- The system safely falls back to the active baseline version without operator intervention.
